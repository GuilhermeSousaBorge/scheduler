# Scheduler — Diário de bordo (até 13/11/2026)

Registro de estudo: o que foi construído, decidido e aprendido. As decisões formais estão em [`adr/`](adr/); as convenções para o dia a dia, no [`CLAUDE.md`](../CLAUDE.md).

Sistema de agendamento para lava-jato / estética automotiva. Projeto de portfólio para vagas de backend Java.
Stack: Java 21, Spring Boot 4, PostgreSQL (Docker), Flyway, Spring Security 7 (resource server JWT), JUnit 5 + MockMvc.

---

## 1. Domínio

**O que torna lava-jato diferente de barbearia**
- O recurso limitado é o **box**, não a pessoa.
- **Duração e preço dependem de serviço × porte do veículo** (SUV demora mais e custa mais que hatch).
- Existem serviços **longos** (polimento, vitrificação: o dia todo ou mais).
- O cliente tem **veículos**, e o histórico por veículo tem valor.

**Escopo do MVP**: um estabelecimento (modelado pensando em multi-tenant), serviços com preço/duração por categoria, capacidade por box (sem alocar funcionário), reserva protegida contra conflito, cancelamento.

**Papéis** (hierarquia `ADMIN > ATTENDANT > CLIENT`, um papel por usuário)

| | Cliente | Atendente | Admin |
|---|---|---|---|
| ver horários disponíveis | ✅ | ✅ | ✅ |
| agendar o próprio carro | ✅ | ✅ | ✅ |
| ver/editar os **próprios** agendamentos | ✅ | ✅ | ✅ |
| ver/editar **todos** os agendamentos (inclusive status) | ❌ | ✅ | ✅ |
| ver o perfil de **outros** usuários | ❌ | ✅ | ✅ |
| configurar o sistema (expediente, boxes, catálogo, usuários) | ❌ | ❌ | ✅ |
| alterar o papel de outro usuário (nunca o próprio) | ❌ | ❌ | ✅ |

Dois tipos de regra:
- **Por papel** ("só atendente vê todos") → Spring Security.
- **Por dono** ("cliente só edita o que é dele") → verificação de propriedade no **service**. Papel não resolve, porque dois clientes têm o mesmo papel.

---

## 2. Modelagem

**Entidades**
- `usuario`: nome, telefone (único), senha (hash), role
- `veiculo`: marca, modelo, placa (única), categoria, usuarioId
- `servico`: nome, descrição
- `catalogo`: servicoId, categoria, duração, valor → **chave única (servicoId, categoria)**
- `box`: nome
- `agendamento`: início, fim, status, total, veiculoId, boxId
- `agendamento_item`: agendamentoId, servicoId, valor
- `configuracao`: abertura, fechamento, fuso (e passo)

**Relacionamentos** (regra: cada campo `...Id` é um 1:N; o lado "1" é a tabela apontada)
- usuário 1:N veículo · veículo 1:N agendamento · box 1:N agendamento
- agendamento 1:N item · serviço 1:N item · serviço 1:N catálogo

**Decisões e porquês**
- **Catálogo = N:N entre serviço e categoria com atributos.** Mesmo padrão do item do agendamento.
- **Snapshot do preço no item.** O preço do catálogo muda; o agendamento feito não pode mudar, e o faturamento de meses passados também não. O item guarda o valor praticado; o catálogo guarda o valor de tabela.
- **Início e fim como instantes completos** (não `data` + `hora`), e o fim **determinístico** (início + duração), nunca "aproximado". Senão a garantia de não-conflito vira aproximada também.
- **Lista não é coluna.** `lista_veiculos` não existe em banco relacional: o 1:N é a FK do lado N.
- **Categoria como enum** no MVP (menos complexidade). No multi-tenant, uma tabela começa a ganhar (cada tenant com suas categorias).
- **Role como enum, e não tabela.** Critério: *o código toma decisões com base nesse valor?* Se sim → enum (é comportamento, mora no código). Se é só dado → tabela. Papel criado por tenant exigiria RBAC completo (permissões + vínculo).
- **Multi-tenant (futuro):** o papel pertence à relação usuário × estabelecimento, não ao usuário.

---

## 3. Algoritmo de disponibilidade

**Sobreposição de intervalos** `[início, fim)` (inclui o início, exclui o fim)
- Não se sobrepõem: `fimA <= inicioB` **ou** `fimB <= inicioA`
- Negando (De Morgan): **conflita** ⟺ `fimA > inicioB` **e** `fimB > inicioA`
- Encostar não é conflito: 8h–9h30 e 9h30–10h30 **não** conflitam.

**Pseudocódigo**
```
duração  = catálogo(serviço, categoria do veículo)
abertura, fechamento, fuso = configuração
para cada box:
    agendamentosDoBox = do box, no dia pedido (no fuso do lava-jato),
                        com status em [AGENDADO, EM_USO]
    t = dia + abertura (no fuso)
    enquanto t + duração <= fechamento:
        fimCandidato = t + duração
        conflita = falso                       // flag
        para cada ag em agendamentosDoBox:
            se ag.fim > t e fimCandidato > ag.inicio:
                conflita = verdadeiro
                break                          // sai só do laço interno
        se não conflita: guarda (box, t)
        t = t + passo
retorna os guardados
```

**Lições**
- **Ordem importa**: primeiro a duração (serviço × categoria), depois "lotado?". "Lotado" depende da duração.
- **Olhar o box, não o dia.** 14h pode ter dois boxes ocupados e um livre.
- **Flag começa em falso**: senão um box vazio nunca ofereceria horários.
- **`break` sai do laço mais próximo**; `return` encerraria o método.
- **Status em lista explícita** (`[AGENDADO, EM_USO]`) é mais seguro que "diferente de CANCELADO": um status novo não bloqueia nada sem decisão consciente.
- **O algoritmo recebe dados prontos** (duração, agendamentos, boxIds, configuração) e não busca no banco → é função pura, testável sem Spring.
- **Valores intermediários ganham nome** (`fimCandidato`): variável com nome bom é memória que você não carrega na cabeça.

---

## 4. `java.time`

| Pergunta | Tipo |
|---|---|
| Que dia? | `LocalDate` |
| Que hora do dia? (abertura/fechamento) | `LocalTime` |
| Dia + hora, sem fuso | `LocalDateTime` |
| Qual fuso? | `ZoneId` |
| Momento exato no mundo | `Instant` / `OffsetDateTime` / `ZonedDateTime` |
| Quanto tempo (h, min, s)? | `Duration` |
| Quanto tempo (anos, meses, dias)? | `Period` |

- `LocalDate` + `LocalTime` + `ZoneId` → `ZonedDateTime.of(...)` (o candidato `t`).
- `OffsetDateTime` guarda um deslocamento fixo (`-03:00`); `ZonedDateTime` guarda a região e suas regras (horário de verão).
- Guardar e transportar: `Instant`/`OffsetDateTime` ↔ `timestamptz`. Calcular com calendário: `ZonedDateTime`.
- Para extrair a data no fuso do lava-jato: `withZoneSameInstant(fuso).toLocalDate()`.
- Não existe `>`/`<=` entre datas: `isAfter`, `isBefore`; "menor ou igual" = `!isAfter`.
- Armazenamento e tipo de domínio são decisões separadas (pode guardar minutos no banco e usar `Duration` no código).

---

## 5. Arquitetura

**Três tipos de objeto, três fronteiras**

| Tipo | Papel | Fronteira |
|---|---|---|
| Entidade JPA | espelha a tabela | banco ↔ aplicação |
| Record de domínio | o que o algoritmo entende | núcleo |
| DTO | o que a API recebe e devolve | aplicação ↔ cliente HTTP |

- Os records do algoritmo impedem o **banco de vazar para dentro do núcleo**; os DTOs impedem o núcleo de vazar para fora.
- **Package by feature** (`user/`, `agendamento/`, `disponibilidade/`, `shared/`). O que é de todos fica em `shared`. Exceções específicas de uma feature moram na feature.
- **Mapper** só para a saída (entidade → DTO). A entrada (DTO → entidade) fica no service quando depende de beans (ex.: `PasswordEncoder`).
- **Idioma do código: inglês.** Mensagens ao usuário: português.

---

## 6. Entidade rica e value objects

- **Sem `@Setter`, `@AllArgsConstructor`, `@Builder` na entidade.**
  - Setter = porta dos fundos para quebrar invariantes (modelo anêmico).
  - `@AllArgsConstructor` expõe `id`/`createdAt` e depende da ordem dos campos (dois `String` trocados compilam e corrompem dados).
  - `@Builder` deixa construir objeto incompleto.
- **Regra: o objeto nasce válido e só muda por caminhos que o mantêm válido.** Mudanças por métodos com intenção (`changeRole(...)`, `alterarTelefone(...)`).
- **Construtor vazio `protected`** (`@NoArgsConstructor(access = AccessLevel.PROTECTED)`): o Hibernate usa, o código não.
- **`id`, `createdAt`, `updatedAt` não entram no construtor**: quem preenche é o banco/Hibernate.
- **Role definido dentro do construtor** (`CLIENT`), nunca recebido → bloqueia *mass assignment*.
- **`Objects.requireNonNull`** para argumentos que só chegam nulos por bug → vira 500 (correto), e não um 409 enganoso vindo do `not null` do banco.
- **`Phone` como record** (comparação por valor, imutável). **`Password` como classe** com construtor privado + `create(raw, encoder)` / `fromHash(hash)`: em record o construtor é sempre público e o `toString()` exporia o hash nos logs.
- **`static` × instância**: `static` é para o que não depende de um objeto (fábricas, validadores). Ler um dado é sempre de instância (`user.getPhone().number()`).
- **`EnumType.STRING`**, nunca `ORDINAL` (reordenar o enum trocaria o papel de todo mundo em silêncio).

---

## 7. Exceções e erros da API (RFC 9457 / `ProblemDetail`)

**Hierarquia**
- `BusinessException` (abstrata, `RuntimeException`, carrega `HttpStatus`)
  - `InvalidDataException` (400), `ResourceNotFoundException` (404) em `shared`
  - `PhoneAlreadyRegisteredException` (409) em `user`
  - `UserNotFoundException` (404) e `CannotChangeMyOwnRoleException` (422) em `user`
- A **subclasse decide status e mensagem**; quem lança só escreve `throw new X()`.
- `RuntimeException`: sem `throws` em cascata e é o que dispara rollback do `@Transactional`.
- Trade-off: `HttpStatus` na exceção acopla domínio à web (pragmático e defensável).

**`GlobalExceptionHandler` (`@RestControllerAdvice` + `extends ResponseEntityExceptionHandler`)**
- `BusinessException` → status e mensagem da própria exceção.
- `DataIntegrityViolationException` → 409, mensagem genérica, log `warn`.
- `Exception` (fallback) → 500, mensagem genérica, log `error`.
- `MethodArgumentNotValidException` → 400 com `errors: { campo: mensagem }` (sobrescrito, via `handleExceptionInternal`).
- Erros do próprio Spring (JSON malformado, 405, rota inexistente) → herdados da base.
- `NoResourceFoundException` (rota inexistente) sobrescrito → 404 "Recurso não encontrado" (a mensagem padrão expunha "static resource").
- `AuthenticationException` → 401 genérico; `AccessDeniedException` do Spring Security → 403.
- Não criar handler específico para subclasse de `BusinessException`: duplicaria a mensagem.

**Regras**
- **Nunca** `ex.getMessage()` na resposta de erros não previstos (vaza SQL, tabela, assinatura de método).
- `IllegalArgumentException` = quem chamou passou valor ruim. `IllegalStateException` = o sistema está inconsistente (ex.: hash vazio vindo do banco → 500, não 400).
- `InternalError` é `Error` da JVM, não exceção de aplicação.
- **Check-then-act**: `existsByPhone` dá a mensagem amigável; quem garante de verdade é o `unique` do banco (tratado como 409).
- Nem toda `DataIntegrityViolationException` é duplicidade (FK, not null, tamanho) → mensagem genérica.

---

## 8. Segurança

**Base**
- **CSRF desligado** porque a API é stateless com token no header (CSRF protege apps com sessão/cookie). Sessão `STATELESS`.
- **Rotas públicas** (filtradas por método + caminho): `POST /api/users`, `POST /api/auth/login` e Swagger. Todo o resto `authenticated()`.
- **Segredo**: `JWT_SECRET` (≥ 32 **bytes**, validado na subida, `getBytes(UTF_8)`); expiração `JWT_EXPIRATION` (padrão `1h`, injetada como `Duration`).
- **HS256 (chave simétrica)** basta num monolito. Par de chaves: a **privada assina** (só o emissor tem), a **pública verifica** → microsserviços validam sem poder forjar.

**Validação de token (resource server)**
- `spring-boot-starter-oauth2-resource-server` + `.jwt(Customizer.withDefaults())` + beans `JwtDecoder`/`JwtEncoder`/`JwtAuthenticationConverter` (estilo implícito: o Spring acha os beans).
- Ele extrai o Bearer, verifica assinatura e `exp`, monta a autenticação e responde 401.
- `JwtGrantedAuthoritiesConverter`: claim `roles`, prefixo `ROLE_` (sem isso usa `SCOPE_` e todo `hasRole` dá 403).
- `RoleHierarchy` via builder (`withDefaultRolePrefix().role(...).implies(...)`); hierarquia é transitiva.

**Login**
- `AuthenticationManager` (via `AuthenticationConfiguration`) + `DaoAuthenticationProvider` + `UserDetailsServiceImpl` + `CustomUserDetails` (carrega o id).
- O `DaoAuthenticationProvider` já protege contra **timing attack** (roda um BCrypt falso quando o usuário não existe) e transforma usuário inexistente e senha errada na mesma `BadCredentialsException` — **desde que** o `loadUserByUsername` lance `UsernameNotFoundException` (qualquer outra exceção pula a proteção).
- Erro de credenciais: **401** com mensagem genérica ("Telefone ou senha incorretos").
- Token: `iss`, `sub` = **id** do usuário (nunca o telefone), `iat`, `exp`, `roles` montado a partir do `Role` (as authorities incluem `FACTOR_PASSWORD` no Spring Security 7, que não deve ir para o token).
- `JwsHeader.with(MacAlgorithm.HS256)` ao emitir (o encoder tenta RS256 por padrão).
- Resposta: `{ user, token, expiresIn }` (segundos). A expiração vem de uma fonte só (`TokenService`).
- **Decodificar o token** (jwt.io) é parte do teste: foi assim que apareceram o `sub` errado e o `FACTOR_PASSWORD`.

**401 e 403 em `ProblemDetail` nos dois caminhos**
- Filtros (antes do Spring MVC): `CustomAuthenticationEntryPoint` (401) e `CustomAccessDeniedHandler` (403), registrados em `exceptionHandling` **e** em `oauth2ResourceServer`. Escrevem o JSON direto (Jackson 3, `tools.jackson`); nunca `sendError`.
- Spring MVC: `AuthenticationException` → 401 e `AccessDeniedException` **do Spring Security** (`org.springframework.security.access`) → 403 no `GlobalExceptionHandler`. Sem esse handler, a negação do `@PreAuthorize` caía no fallback e virava 500.
- `AuthenticationEntryPoint` = "não sei quem você é" (401). `AccessDeniedHandler` = "sei quem você é, mas não pode" (403).

**Outros**
- **BCrypt** é lento de propósito → verificar telefone **antes** de gerar hash. Limite de 72 bytes da senha.
- **Primeiro admin** por `ApplicationRunner` (ADR 0011).
- *User enumeration*: o cadastro (409 "telefone já cadastrado") já revela contas; risco aceito aqui.
- Spring Security 7: métodos sem argumento + `.and()` são do estilo antigo (removidos). Use lambdas/`Customizer`.

**Cookie × header (discussão de 12/11)**
- Cookie `HttpOnly` impede o XSS de **roubar** o token, mas não de **usá-lo**; traz de volta o CSRF (o navegador envia o cookie sozinho), exige CORS com credenciais e um `BearerTokenResolver` próprio.
- *Origem* ≠ *site*: `localhost:3000` e `localhost:8080` são origens diferentes, mas o mesmo site para o `SameSite`.
- Plano: access token curto em memória (header) + refresh token em cookie `HttpOnly`/`Secure`/`SameSite=Strict` com `Path` restrito (ADR 0013). Alternativa: BFF no Next.js.

---

## 8.1 Autorização

- **Regras só de papel** na fronteira: `@PreAuthorize("hasRole('ADMIN')")`, que só funciona com `@EnableMethodSecurity` (sem ele, a anotação é ignorada em silêncio — e qualquer logado passava).
- **Regras de dono** no service (Java comum, testável com Mockito). Trade-off: no service é fácil esquecer a checagem num método novo → a tabela de testes por endpoint é a rede de proteção.
- Controller: `@AuthenticationPrincipal Jwt jwt` → `CurrentUser(Long.parseLong(jwt.getSubject()), Role.valueOf(jwt.getClaimAsStringList("roles").getFirst()))`. Prefira getters tipados ao `getClaim` genérico (ele deixa você "mentir" para o compilador). `jwt.getId()` é o `jti`, não o usuário.
- **IDOR / BOLA** (OWASP API #1): sem regra de dono, `GET /api/users/1` devolvia o perfil de qualquer um.
- Recurso de outro → **404 idêntico** ao inexistente (mesma `UserNotFoundException`), checado **antes** do banco.
- Escrever a regra positiva com nome: `canView = isOwner || role.canViewOtherProfiles()`; barrar com `!canView` (evita De Morgan de cabeça).
- *Fail-closed*: listar quem **pode**, não quem não pode. `switch` exaustivo sem `default` no enum: papel novo não compila até alguém decidir.
- Troca de papel: `PATCH /api/users/{id}/role` (primeiro foi `/change-role`, rota de ação; trocada para `/role` por consistência com o estilo REST do resto da API), DTO só com `@NotNull Role role`, `204`. Admin não altera o próprio papel → 422 (`CannotChangeMyOwnRoleException`); isso também protege o último admin. `changeRole` na entidade é `void` e usa o `this`; o *dirty checking* grava sem `save`.
- DTO de cada endpoint contém só os campos que aquele endpoint pode alterar (nunca `role` no cadastro/perfil → evita *mass assignment*).
- Token antigo mantém o papel antigo até expirar: quem é promovido precisa logar de novo.

---

## 9. REST

- Recurso no plural, verbo no método HTTP: `POST /api/users` (não `/api/user/create`).
- **201 Created + `Location`**: `ResponseEntity.created(uri).body(dto)`, com `ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri()`. Passar o **id**, não o DTO (o `toString` iria para o header).
- Usar o **retorno do `save`**.
- **Sem envelope `{ data }`** para recurso único (o HTTP já é o envelope; erros ficam em `ProblemDetail` de qualquer jeito). Listas terão `PageResponse` com metadados reais.
- **Nunca lista sem limite**: `Pageable` desde o primeiro endpoint de listagem, com tamanho máximo configurado.
- Nunca expor senha/hash no DTO de saída.
- `PUT` substitui o recurso inteiro (idempotente); `PATCH` altera parte. Evitar o "PUT parcial" (campo ausente vira ambíguo).
- `204 No Content` (`ResponseEntity.noContent().build()`) quando não há corpo.
- `@Valid` só faz efeito em objetos (DTO no `@RequestBody`); num `Long` de path não faz nada.
- Rotas: `POST /api/users`, `GET /api/users/{id}`, `PATCH /api/users/{id}/role`, `POST /api/auth/login`.

---

## 10. Infra e configuração

- **Flyway** cria e altera tabelas; Hibernate só valida (`ddl-auto: validate`). Nunca `update` junto.
- **Migration aplicada num ambiente que importa nunca é editada** → nova versão (`V2`, `V3`...).
- `ADD COLUMN ... NOT NULL DEFAULT x` preenche as linhas existentes. `CHECK (role IN (...))` garante valores válidos no banco.
- Tipos: `timestamptz` para instantes; aspas simples para texto e duplas para identificadores no Postgres; nome de constraint com convenção (`ck_tb_user_role`); `unique` já cria índice.
- `db/migration` são **duas pastas** (`db` → `migration`), não uma com ponto no nome. Flyway 10+ precisa de `flyway-database-postgresql`.
- **Perfis**: `application.yaml` (base, `spring.profiles.active: ${SPRING_PROFILES_ACTIVE:develop}`), `application-develop.yaml`, `application-production.yaml` (só `${VARIÁVEIS}`, pode ir para o Git). `open-in-view: false`, `hibernate.jdbc.time_zone: UTC`.
- Postgres via Docker Compose; `.env` fora do Git, `.env.exemplo` documentando as variáveis.
- Spring Boot 4 usa **Jackson 3** (pacote `tools.jackson`), não `com.fasterxml`.
- `mvnw` precisa de permissão de execução no Git (`git update-index --chmod=+x mvnw`).

---

## 11. Testes

- **Arrange / Act / Assert**; nome do teste descreve o comportamento garantido.
- **O teste descreve o que deveria acontecer, não o que o código faz.** Teste vermelho → primeiro perguntar "quem está errado, o código ou o esperado?".
- Um teste, um comportamento; cenário mínimo.
- `assertEquals` com listas de records funciona porque records geram `equals`.
- **`@WebMvcTest`** + controller de teste que só lança exceções + `@WithMockUser` → testa o handler sem banco.
- Verificação explícita de "não vaza" (`not(containsString(...))`) documenta a intenção.
- Função pura (algoritmo) não precisa de Mockito. **Mockito** entra no service (repository simulado).
- IA pode escrever o boilerplate do teste; **os cenários e os resultados esperados são seus**.

---

## 12. Armadilhas que já aconteceram (erros silenciosos)

- Falta de anotação: `@Test`, `@ExceptionHandler`, `@Configuration` → compila, sobe e não faz nada.
- `@ExceptionHandler(Exception.class)` com parâmetro `BusinessException` → quebra o tratamento de erros.
- `@Value` em campo `static` → `null`; `@Value("X")` sem `${}` → texto literal.
- `ChronoZonedDateTime.from(localDate)` / `OffsetDateTime.from(localDate)` → exceção em runtime.
- `JpaRepository<User, Integer>` com id `Long`.
- Regex com `\\+d{2}` (faltou a barra) e `timestampz` (é `timestamptz`).
- Perfil `dev` × `develop` ao copiar configuração de outro projeto.
- Dois beans do mesmo tipo (`PasswordEncoder`) → aplicação não sobe.
- `@NotBlank` em enum (só serve para texto) → exceção de configuração; use `@NotNull`. `@Valid` esquecido → validação nunca roda.
- `@EnableMethodSecurity` ausente → `@PreAuthorize` ignorado (brecha de segurança).
- Classe própria com o nome de uma do Spring (`AccessDeniedException`) → handler esperando exceção que ninguém lança.
- `lombok.Value` no lugar do `@Value` do Spring; `Long` injetado a partir de `"1h"`; chave a mais no yaml (`${X}}`).
- `@RequiredArgsConstructor` com campo sem `final` → bean `null`.
- Cast de `User` para `UserDetails` (compila, explode em runtime).
- `getClaim("sub")` atribuído a `Long` (é `String`); claim `role` × `roles`.
- `+` tem precedência sobre `==`: `"texto" + x == null ? ...` nunca é verdadeiro → mensagem vazia.
- Testes: chamar o mock em vez do service (tautológico); comparar exceções com `equals` (identidade); comparar `Phone` com `String` (`assertEquals(Object, Object)` não avisa); `||` no lugar de `&&` ao negar uma regra.
- **Antídoto**: rodar `./mvnw compile`/testes antes do push e **provar que roda** disparando o caso.

---

## 13. Git

- Commits pequenos com mensagem que diz o que mudou (`fix: corrige tipo timestamptz e regex do Phone`).
- Evitar merges vazios: `git pull --rebase` (ou `pull.rebase true`) num projeto solo.
- O que é segredo vai no `.gitignore` (`.env`); configuração sem segredo vai para o Git.

---

## 14. Estado atual e próximos passos

**Pronto**
- Algoritmo de disponibilidade + testes JUnit (código ainda nos pacotes legados `record/`, `enums/`, `services/`).
- Usuário: entidade rica, `Phone`/`Password`, cadastro (`201` + `Location`), papel `CLIENT` por padrão (V2), perfil com regra de dono (404 para IDOR), troca de papel pelo admin, primeiro admin automático.
- Login com JWT (HS256), claims revisadas, `expiresIn`.
- Tratamento global de erros em `ProblemDetail`, inclusive 401/403 nos filtros e no MVC; padrão de mensagens definido (`title` classifica, `detail` explica).
- Testes: handler (MockMvc), `UserService` (Mockito: perfil — dono, outro cliente, atendente, admin, inexistente).

**Pendências**
- **`GET /api/users` está aberto a qualquer usuário logado e sem paginação** (expõe nome e telefone de todos): restringir por papel e paginar.
- Testes do `changeRole` no `UserService` (próprio papel → 422 sem consultar o banco; inexistente → 404; caso feliz → papel alterado no objeto).
- Reorganizar `record/`, `enums/`, `services/` em `disponibilidade/`; renomear o record `Agendamento` (ex.: `Ocupacao`) e remover `Box` sem uso.
- `@Size(max)` da senha (72 bytes do BCrypt).
- `HandlerMethodArgumentResolver` para `CurrentUser` quando a conversão aparecer no terceiro controller.
- **Domínio**: migration de box, serviço, catálogo, veículo, agendamento e itens (tipos para dinheiro e duração), endpoint de disponibilidade, constraint de exclusão do Postgres contra reserva dupla.
- Decidir: atendente agenda para cliente sem conta?
- Algoritmo: não oferecer horário passado (`Clock`); expediente por dia da semana; passo configurável.
- Refresh token (desenho da ADR 0013); rate limiting no login; CORS quando o front entrar.

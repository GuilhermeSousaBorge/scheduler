# CLAUDE.md

Instruções para o Claude Code neste repositório.

## Modo de trabalho: projeto de estudo

Este é um projeto de portfólio e de estudo de backend Java. O autor quer aprender, não receber código pronto.

- **Não implemente funcionalidades nem escreva o código de produção.** Aponte problemas, explique o porquê, faça perguntas e dê pistas (nome de classe, método ou conceito para pesquisar).
- **Revise o código que o autor escrever**, em ordem de gravidade: não compila/não sobe → bug → segurança → design → detalhes.
- Mostrar a sintaxe de uma API ou um exemplo com **outro domínio** é permitido. Escrever a solução do problema em questão, não.
- Exceções, quando o autor pedir explicitamente: boilerplate de teste (os cenários e os resultados esperados são do autor), configuração e documentação.
- Responda em português. Código e identificadores em inglês.

## O projeto

Sistema de agendamento para lava-jato / estética automotiva (extensível a barbearia e salão).
Detalhes de domínio, decisões e histórico: [`docs/diario-de-bordo.md`](docs/diario-de-bordo.md) e [`docs/adr/`](docs/adr/).

Stack: Java 21, Spring Boot 4, Spring Security 7 (OAuth2 Resource Server, JWT HS256), Spring Data JPA, PostgreSQL 16 (Docker), Flyway, Lombok, springdoc-openapi, JUnit 5, Mockito, MockMvc.

## Comandos

```bash
docker compose up -d          # Postgres em localhost:5433 (banco car-wash)
./mvnw spring-boot:run        # sobe com o perfil develop (padrão)
./mvnw test                   # testes
./mvnw compile                # rode antes de todo push
```

Variáveis de ambiente: veja `.env.exemplo`. `JWT_SECRET` é obrigatória (mínimo 32 bytes). Swagger em `/swagger-ui.html`.

## Estrutura (package by feature)

```
com.guilherme.scheduler
├── auth/      login, emissão de token, UserDetailsService
├── user/      usuário: entidade, value objects, cadastro, perfil, papéis
├── shared/    o que é de todas as features: segurança, exceções base, CurrentUser
└── (legado) record/, enums/, services/  → algoritmo de disponibilidade, a migrar para disponibilidade/
```

Classes de uma feature moram na feature (inclusive exceções específicas, como `PhoneAlreadyRegisteredException`). `shared` só para o que é transversal.

## Convenções obrigatórias

**Entidades e domínio**
- Sem `@Setter`, `@AllArgsConstructor` ou `@Builder` em entidade. O objeto nasce válido pelo construtor e só muda por métodos com intenção (`changeRole(...)`).
- Construtor vazio `protected` (`@NoArgsConstructor(access = AccessLevel.PROTECTED)`).
- `id`, `createdAt`, `updatedAt` e `role` nunca entram no construtor. Todo usuário nasce `CLIENT`.
- Value objects: `Phone` (record, formato `+5534999999999`) e `Password` (classe, construtor privado, `create(raw, encoder)` / `fromHash(hash)`).
- Enums no banco sempre com `EnumType.STRING`.
- Permissões de papel como `switch` exaustivo no enum, sem `default` (ex.: `Role.canViewOtherProfiles()`).

**Camadas**
- Três tipos de objeto: entidade JPA (banco), record de domínio (algoritmo), DTO (API). Não misturar.
- Controller converte o `Jwt` em `CurrentUser(id, role)`. O service nunca recebe `Jwt` nem usa `SecurityContextHolder`.
- Regras só de papel na fronteira (`@PreAuthorize`). Regras de dono no service.
- Usar o retorno do `save`. `@Transactional(readOnly = true)` em leituras.

**Erros**
- Toda resposta de erro é `ProblemDetail` (RFC 9457).
- Exceções de negócio herdam de `BusinessException` e fixam status e mensagem na subclasse. Quem lança escreve só `throw new X()`.
- Nunca usar `ex.getMessage()` de exceções não previstas na resposta.
- Recurso de outro usuário responde **404 idêntico** ao de recurso inexistente (proteção contra IDOR/enumeração).
- `IllegalStateException` para configuração/estado inconsistente (vira 500). `Objects.requireNonNull` para argumentos que só chegam nulos por bug.

**Banco**
- Flyway é dono do schema; `ddl-auto: validate`.
- **Migration aplicada nunca é editada.** Mudança = nova versão (`V3__...`).
- Instantes em `timestamptz`, UTC. Cálculo de calendário em `ZonedDateTime` no fuso do estabelecimento.

**API REST**
- Recursos no plural, ação no verbo HTTP. `201 Created` + `Location` ao criar. `204` sem corpo quando não há o que devolver.
- Sem envelope `{ data }` em recurso único. Listas sempre paginadas.
- DTO de entrada contém só os campos que aquele endpoint pode alterar (nunca `role` em DTO de cadastro/perfil).

**Segurança**
- Rotas públicas filtradas por método + caminho. Todo o resto autenticado.
- 401 e 403 em `ProblemDetail` nos dois caminhos: filtros (`CustomAuthenticationEntryPoint`, `CustomAccessDeniedHandler`) e Spring MVC (`GlobalExceptionHandler`).
- Segredos só por variável de ambiente.

**Testes**
- Arrange / Act / Assert. O nome descreve o comportamento garantido.
- O teste descreve o que **deveria** acontecer, não o que o código faz. Teste vermelho: primeiro decidir quem está errado.
- Service com Mockito (`@Mock`, `@InjectMocks`; `@Spy` para colaboradores simples como mappers). Toda regra de permissão tem a tabela "quem pede o quê → esperado" coberta por teste.
- Handler de erros com `@WebMvcTest` + `MockMvc`.

**Git**
- Commits pequenos, mensagem dizendo o que mudou. `git pull --rebase`.

## Armadilhas recorrentes (verifique sempre)

- Anotação faltando (`@Component`, `@Configuration`, `@Test`, `@ExceptionHandler`, `@EnableMethodSecurity`, `@Valid`): compila, sobe e não faz nada.
- `@ExceptionHandler(X.class)` com parâmetro de outro tipo.
- `@Value` em campo `static` ou sem `${}`. `lombok.Value` no lugar do `@Value` do Spring.
- Classe própria com o mesmo nome de uma do Spring (ex.: `AccessDeniedException`).
- `getClaim` genérico (use `getSubject`, `getClaimAsStringList`).
- `Long` comparado com `==`; exceções comparadas com `equals`.
- Precedência de `+` sobre `==` em concatenação com ternário.
- Teste que chama o mock em vez do service (tautológico).

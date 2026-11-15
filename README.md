# Scheduler

API de agendamento para lava-jato e estética automotiva: clientes e atendentes consultam horários livres por box e reservam serviços cuja duração e preço dependem do porte do veículo. Projeto de portfólio de backend Java.

## Stack

Java 21 · Spring Boot 4 · Spring Security 7 (OAuth2 Resource Server, JWT) · Spring Data JPA · PostgreSQL 16 · Flyway · springdoc-openapi · JUnit 5 · Mockito · MockMvc

## Como rodar

Pré-requisitos: JDK 21 e Docker.

```bash
cp .env.exemplo .env           # preencha JWT_SECRET (mínimo 32 bytes) e, na primeira subida, ADMIN_*
docker compose up -d           # PostgreSQL em localhost:5433
./mvnw spring-boot:run         # perfil develop por padrão
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- Testes: `./mvnw test`

| Variável | Uso |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `develop` (padrão) ou `production` |
| `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` | banco em produção |
| `JWT_SECRET` | segredo HS256, mínimo 32 bytes |
| `JWT_EXPIRATION` | validade do token (padrão `1h`) |
| `ADMIN_NAME`, `ADMIN_PHONE`, `ADMIN_PASSWORD` | criação do primeiro admin, só se ainda não existir nenhum |

## Endpoints atuais

| Método | Rota | Acesso |
|---|---|---|
| `POST` | `/api/users` | público — cadastro (sempre como cliente) |
| `POST` | `/api/auth/login` | público — devolve o JWT |
| `GET` | `/api/users/{id}` | dono, atendente ou admin |
| `PATCH` | `/api/users/{id}/role` | admin (não pode alterar o próprio papel) |

Erros sempre em `application/problem+json` (RFC 9457).

## Destaques técnicos

- **Algoritmo de disponibilidade** como função pura, coberto por testes: sobreposição de intervalos semiabertos por box, filtro por status e fuso do estabelecimento.
- **Entidade rica e value objects** (`Phone`, `Password`): o objeto nasce válido e não tem setters.
- **Autorização em camadas:** papel na fronteira (`@PreAuthorize`), dono no service; recursos de outros usuários respondem 404 idêntico ao inexistente (proteção contra IDOR).
- **Login** com `DaoAuthenticationProvider` (proteção contra timing attack e enumeração de usuários).
- **Erros padronizados** em `ProblemDetail`, inclusive 401/403 gerados nos filtros do Spring Security.
- **Flyway** como dono do schema; Hibernate só valida.

## Documentação

- [`docs/adr/`](docs/adr/) — decisões de arquitetura (contexto, decisão, consequências)
- [`docs/diario-de-bordo.md`](docs/diario-de-bordo.md) — diário de estudo: o que foi construído e aprendido
- [`CLAUDE.md`](CLAUDE.md) — convenções do projeto e instruções para o Claude Code

## Status

Em desenvolvimento. Fundação (usuários, autenticação, autorização, erros) concluída; próximo passo é o domínio (boxes, catálogo, veículos, agendamentos) e o endpoint de disponibilidade.

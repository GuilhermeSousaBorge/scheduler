# 0009 — Papéis como enum, hierarquia e papel no token

**Status:** Aceita

## Contexto
Papéis: cliente, atendente, admin. Seria possível uma tabela de papéis criados pelo usuário, mas o código toma decisões com base no papel.

## Decisão
- Critério: **se o código decide com base no valor, é enum; se é só dado, é tabela.** Papéis são comportamento → enum `Role { CLIENT, ATTENDANT, ADMIN }`, coluna `varchar` com `CHECK`, `EnumType.STRING`.
- Um papel por usuário, hierarquia `ADMIN > ATTENDANT > CLIENT` via `RoleHierarchy` (builder com `Role.X.name()`).
- Permissões específicas como métodos do enum com `switch` exaustivo e sem `default` (ex.: `canViewOtherProfiles()`): um papel novo não compila até alguém decidir.
- O papel vai no token (claim `roles`), evitando consulta ao banco a cada requisição.

## Consequências
- Papel novo exige alteração de código e migration.
- Quando o papel de alguém muda, o token antigo continua com o papel anterior até expirar (janela = expiração do token).
- No multi-tenant futuro, o papel passa a pertencer ao vínculo usuário × estabelecimento.

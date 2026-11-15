# Architecture Decision Records

Cada ADR registra uma decisão: o contexto, o que foi decidido e as consequências (inclusive as negativas).
Formato curto, inspirado em Michael Nygard. Uma ADR aceita não é editada: se a decisão mudar, cria-se outra que a substitui.

| # | Decisão | Status |
|---|---|---|
| [0001](0001-package-by-feature.md) | Package by feature e três tipos de objeto (entidade, domínio, DTO) | Aceita |
| [0002](0002-flyway-dono-do-schema.md) | Flyway é o dono do schema; migrations imutáveis | Aceita |
| [0003](0003-entidade-rica-e-value-objects.md) | Entidade rica e value objects (`Phone`, `Password`) | Aceita |
| [0004](0004-erros-com-problem-detail.md) | Erros em `ProblemDetail` com hierarquia `BusinessException` | Aceita |
| [0005](0005-tempo-utc-e-fuso-do-estabelecimento.md) | Instantes em UTC; cálculo no fuso do estabelecimento | Aceita |
| [0006](0006-disponibilidade-como-funcao-pura.md) | Algoritmo de disponibilidade como função pura | Aceita |
| [0007](0007-snapshot-de-preco.md) | Snapshot do preço no item do agendamento | Aceita |
| [0008](0008-jwt-resource-server-hs256.md) | Autenticação JWT com o resource server nativo e HS256 | Aceita |
| [0009](0009-papeis-como-enum.md) | Papéis como enum, hierarquia e papel no token | Aceita |
| [0010](0010-autorizacao-em-camadas.md) | Autorização em camadas: papel na fronteira, dono no service, 404 para IDOR | Aceita |
| [0011](0011-primeiro-admin-application-runner.md) | Primeiro admin criado por `ApplicationRunner` | Aceita |
| [0012](0012-formato-das-respostas.md) | Sem envelope de resposta; listas paginadas | Aceita |
| [0013](0013-token-no-header.md) | Token no header `Authorization`; cookies só para o futuro refresh token | Aceita |

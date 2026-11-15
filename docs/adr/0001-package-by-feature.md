# 0001 — Package by feature e três tipos de objeto

**Status:** Aceita

## Contexto
O sistema vai crescer em várias áreas de negócio (usuários, catálogo, boxes, agendamentos, disponibilidade). Organizar por camada (`controllers/`, `services/`, `repositories/`) espalha cada funcionalidade por vários pacotes e não impede uma feature de usar o repositório de outra.

Além disso, o algoritmo de disponibilidade precisa ser testável sem banco, e a API não pode expor detalhes das entidades.

## Decisão
- Pacotes por funcionalidade: `auth/`, `user/`, `shared/` e, a seguir, `disponibilidade/`, `agendamento/` etc. `shared/` guarda apenas o que é transversal (segurança, exceções base, `CurrentUser`).
- Três tipos de objeto, cada um protegendo uma fronteira:
  - **Entidade JPA:** espelha a tabela (banco ↔ aplicação).
  - **Record de domínio:** o que o núcleo/algoritmo entende (impede o banco de vazar para dentro).
  - **DTO:** o que a API recebe e devolve (impede o núcleo de vazar para fora).
- O mapeamento entidade → DTO fica num mapper; DTO → entidade fica no service quando depende de beans (ex.: `PasswordEncoder`).

## Consequências
- Cada feature é lida num lugar só; repositórios podem ficar com visibilidade de pacote.
- Há conversões a mais entre os três tipos (custo aceito pela testabilidade e pelo isolamento).
- O código legado em `record/`, `enums/` e `services/` ainda precisa migrar para `disponibilidade/`.

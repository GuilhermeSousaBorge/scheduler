# 0011 — Primeiro admin criado por ApplicationRunner

**Status:** Aceita

## Contexto
Só um admin promove usuários, então alguém precisa criar o primeiro. Uma migration com o admin colocaria senha (ou hash de senha conhecida) no Git e rodaria em todos os ambientes.

## Decisão
- `AdminInitializer` (`ApplicationRunner`, feature `user`) roda na subida:
  1. se já existe usuário com papel `ADMIN`, não faz nada;
  2. senão, lê `ADMIN_NAME`, `ADMIN_PHONE` e `ADMIN_PASSWORD` (padrão vazio no yaml);
  3. se faltar alguma, falha a subida com `IllegalStateException` listando **todas** as que faltam;
  4. cria o usuário pelo caminho normal e promove com `changeRole(Role.ADMIN)`.
- Um admin não pode alterar o próprio papel; com isso, o último admin nunca é rebaixado (rebaixar exige outro admin).

## Consequências
- Nenhum segredo no repositório; idempotente entre reinícios.
- Após a primeira subida, as variáveis do admin podem ser removidas do ambiente.

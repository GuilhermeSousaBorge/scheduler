# 0002 — Flyway é o dono do schema

**Status:** Aceita

## Contexto
O Hibernate consegue criar e alterar tabelas (`ddl-auto: update`), mas sem histórico, sem revisão e com risco de mudanças destrutivas em produção.

## Decisão
- Todas as alterações de schema são migrations versionadas do Flyway em `src/main/resources/db/migration`.
- O Hibernate só valida o mapeamento (`spring.jpa.hibernate.ddl-auto: validate`).
- **Uma migration aplicada em um ambiente que importa nunca é editada.** Qualquer mudança vira uma nova versão.
- Restrições de integridade moram no banco (`unique`, `check`, FKs): a aplicação valida para dar boas mensagens, o banco garante.

## Consequências
- O schema tem histórico revisável e reproduzível em qualquer ambiente.
- Erros de mapeamento aparecem na subida, não em produção.
- Corrigir uma migration errada já aplicada exige outra migration (ex.: `V2__add_column_role_table_user.sql`).

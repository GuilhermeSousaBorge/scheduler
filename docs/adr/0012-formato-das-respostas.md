# 0012 — Sem envelope de resposta; listas paginadas

**Status:** Aceita

## Contexto
Considerou-se envolver toda resposta em `{ "data": ... }`.

## Decisão
- Recurso único é devolvido diretamente. O HTTP já carrega status e metadados (`Location`, headers), e os erros saem em `ProblemDetail` de qualquer forma, então o envelope não uniformizaria nada.
- Criação: `201 Created` com header `Location`. Operação sem retorno: `204 No Content`.
- Listas: sempre paginadas (`Pageable`), com um `PageResponse` próprio (`data`, `page`, `size`, `total`) e tamanho máximo de página configurado. Nunca expor o `Page` do Spring Data direto.

## Consequências
- O front lê o recurso sem desembrulhar.
- Se um dia houver metadados que não pertençam ao recurso, a decisão será revista.

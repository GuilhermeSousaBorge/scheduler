# 0010 — Autorização em camadas e 404 para recursos de outros

**Status:** Aceita

## Contexto
Há dois tipos de regra: só de papel ("só admin troca papéis") e de dono ("cliente só vê o próprio perfil"). A segunda depende do recurso e não cabe bem numa expressão SpEL. Sem regra de dono, trocar o id na URL expõe dados de outros usuários (IDOR / BOLA, OWASP API #1).

## Decisão
- **Regras só de papel na fronteira:** `@PreAuthorize("hasRole('ADMIN')")` (com `@EnableMethodSecurity`) ou regras por URL.
- **Regras de dono no service**, em Java comum e testadas com Mockito. O controller converte o `Jwt` em `CurrentUser(id, role)`; o service nunca vê o `Jwt`.
- Checagem de permissão **antes** de consultar o banco.
- Recurso de outro usuário responde **404 idêntico** ao inexistente (mesma exceção, ex.: `UserNotFoundException`), sem revelar que existe.
- Regra de negócio violada por quem tem permissão (admin alterando o próprio papel) → 422.

## Consequências
- Regras de dono são fáceis de testar e expressivas.
- Risco: um método novo de service pode esquecer a checagem. Mitigação: para cada endpoint, testes da tabela "quem pede o quê → esperado".

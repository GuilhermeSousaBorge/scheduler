# 0013 — Token no header Authorization

**Status:** Aceita

## Contexto
Alternativa avaliada: enviar o JWT num cookie `HttpOnly`, que o JavaScript não lê (protege contra roubo do token via XSS). Custos: o navegador envia cookies automaticamente, o que traz de volta o CSRF; CORS exige `allowCredentials` e origens explícitas; o resource server precisaria de um `BearerTokenResolver` próprio.

## Decisão
- Por ora, o access token viaja no header `Authorization: Bearer`, e a API segue sem CSRF.
- Desenho planejado para o refresh token: **access token curto em memória** no front + **refresh token em cookie `HttpOnly`, `Secure`, `SameSite=Strict`**, com `Path` restrito à rota de refresh.

## Consequências
- API simples e sem CSRF enquanto o token estiver no header.
- O front deve evitar `localStorage` para o token quando o refresh existir.

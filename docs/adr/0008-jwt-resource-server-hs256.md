# 0008 — JWT com o resource server nativo e HS256

**Status:** Aceita

## Contexto
A API é stateless e consumida por um front separado. Um filtro JWT escrito à mão exige implementar extração do header, verificação de assinatura e de expiração e montagem da autenticação.

## Decisão
- `spring-boot-starter-oauth2-resource-server` valida o token (`.jwt(Customizer.withDefaults())`, beans de `JwtDecoder`, `JwtEncoder` e `JwtAuthenticationConverter`).
- Assinatura **HS256** com segredo simétrico vindo de `JWT_SECRET` (mínimo 32 bytes, verificado na subida). Num monólito, quem emite e quem valida é a mesma aplicação; par de chaves só seria necessário com validadores separados.
- Login via `AuthenticationManager` + `DaoAuthenticationProvider` + `UserDetailsService` próprio (`CustomUserDetails` carrega o id). Isso traz a proteção contra *timing attack* e a mesma exceção para usuário inexistente e senha errada.
- Claims: `iss`, `sub` (id do usuário), `iat`, `exp`, `roles` (montado a partir do `Role`, sem prefixo). Nada pessoal no token.
- Expiração configurável (`JWT_EXPIRATION`, padrão `1h`); a resposta do login informa `expiresIn` em segundos.
- CSRF desligado e sessão `STATELESS`, porque o token viaja no header (ver ADR 0013).

## Consequências
- Menos código de segurança próprio.
- Não há revogação de token: ele vale até expirar. Mitigação planejada: access token curto + refresh token.

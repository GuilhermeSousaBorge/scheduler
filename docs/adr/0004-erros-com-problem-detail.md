# 0004 — Erros em ProblemDetail com hierarquia BusinessException

**Status:** Aceita

## Contexto
Sem um padrão, cada endpoint devolveria erros num formato próprio, e exceções não previstas poderiam vazar SQL, nomes de tabela ou assinaturas de método.

## Decisão
- Todas as respostas de erro usam `ProblemDetail` (RFC 9457).
- Exceções de negócio herdam de `BusinessException` (abstrata, `RuntimeException`, carrega `HttpStatus`). A **subclasse** fixa status e mensagem; quem lança não escolhe nenhum dos dois.
- `GlobalExceptionHandler` (`@RestControllerAdvice`, estende `ResponseEntityExceptionHandler`):
  - `BusinessException` → status e mensagem da exceção;
  - `DataIntegrityViolationException` → 409 genérico (log `warn`);
  - `AuthenticationException` → 401 genérico;
  - `AccessDeniedException` do Spring Security → 403;
  - validação (`@Valid`) → 400 com mapa `errors` campo → mensagem;
  - rota inexistente → 404 com mensagem própria;
  - `Exception` → 500 genérico (log `error`).
- Erros de segurança gerados nos filtros (antes do Spring MVC) são escritos pelo `CustomAuthenticationEntryPoint` (401) e pelo `CustomAccessDeniedHandler` (403), no mesmo formato.
- `title` classifica; `detail` explica o que aconteceu e o que fazer.
- Status `HttpStatus` dentro da exceção acopla o domínio à web — trade-off pragmático aceito.

## Consequências
- Formato único e previsível para o front.
- Mensagens internas nunca saem do servidor.
- Toda exceção nova de negócio é tratada automaticamente pela hierarquia.

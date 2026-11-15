# 0003 — Entidade rica e value objects

**Status:** Aceita

## Contexto
Entidades com setters públicos (modelo anêmico) permitem que qualquer parte do código coloque o objeto num estado inválido. `@AllArgsConstructor` expõe `id` e datas e depende da ordem dos campos; `@Builder` permite objetos incompletos. Telefone e senha têm regras próprias que se repetiriam pelo código.

## Decisão
- Entidades sem `@Setter`, `@AllArgsConstructor` e `@Builder`. O construtor recebe só o que é obrigatório e valida; o construtor vazio é `protected` (uso exclusivo do Hibernate).
- Alterações por métodos com intenção (`User.changeRole(Role)`), que mantêm as invariantes.
- `id`, `createdAt`, `updatedAt` e `role` não entram no construtor. Todo usuário nasce `CLIENT` (bloqueia *mass assignment*).
- Value objects:
  - `Phone` — record, valida o formato `+` seguido de 13 dígitos; comparação por valor.
  - `Password` — classe com construtor privado, `create(raw, encoder)` (valida e gera hash BCrypt) e `fromHash(hash)` (reconstrói do banco). Não é record porque o construtor de um record é sempre público e o `toString()` exporia o hash.
- Persistência com `AttributeConverter` (`PhoneConverter`, `PasswordConverter`).
- Argumentos que só chegariam nulos por bug: `Objects.requireNonNull` (vira 500, não um 409 enganoso vindo do banco).

## Consequências
- Objetos sempre válidos; regras num lugar só.
- Mais código que um POJO com Lombok, e a criação do usuário exige o `PasswordEncoder` (fica no service).

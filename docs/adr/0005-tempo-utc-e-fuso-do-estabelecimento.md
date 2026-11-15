# 0005 — Instantes em UTC; cálculo no fuso do estabelecimento

**Status:** Aceita

## Contexto
Agendamentos são momentos reais no tempo, mas o expediente é definido em horário local ("abre às 8h"), que muda de deslocamento com horário de verão. Gravar data e hora separadas quebra a detecção de conflitos e serviços que atravessam o dia.

## Decisão
- Início e fim de agendamento são instantes completos, gravados em `timestamptz` e em UTC (`hibernate.jdbc.time_zone: UTC`).
- O fim é sempre determinístico: início + duração.
- O fuso do estabelecimento (`ZoneId`) fica na configuração.
- Tipos `java.time`: `LocalDate` (dia), `LocalTime` (abertura/fechamento), `ZoneId` (fuso), `ZonedDateTime` (candidato e cálculo), `Duration` (duração e passo).
- Para saber "o dia" de um agendamento: `withZoneSameInstant(fuso).toLocalDate()`.

## Consequências
- Conflitos e janelas calculados corretamente em qualquer fuso e com mudança de horário.
- Toda conversão para horário local é explícita.

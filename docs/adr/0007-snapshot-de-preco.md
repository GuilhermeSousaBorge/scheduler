# 0007 — Snapshot do preço no item do agendamento

**Status:** Aceita (modelagem; ainda não implementada)

## Contexto
Preço e duração dependem de serviço × categoria do veículo (catálogo, chave única `(servico, categoria)`). O catálogo muda com o tempo; um agendamento já feito e o faturamento de meses passados não podem mudar junto.

## Decisão
- O item do agendamento guarda o **valor praticado** no momento da reserva. O catálogo guarda o valor de tabela.
- O agendamento guarda o fim já calculado, então a duração reservada também fica congelada.

## Consequências
- Reajuste de preço não afeta reservas existentes nem relatórios históricos.
- Há duplicação proposital de dado entre catálogo e item.

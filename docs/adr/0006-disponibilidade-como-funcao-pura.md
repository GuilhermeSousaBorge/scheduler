# 0006 — Algoritmo de disponibilidade como função pura

**Status:** Aceita

## Contexto
Calcular horários livres é o núcleo do sistema e a parte com mais regras. Se o algoritmo buscasse dados no banco, cada teste precisaria de banco e Spring.

## Decisão
- O algoritmo recebe tudo pronto (dia, duração, agendamentos, ids dos boxes, configuração) e devolve `List<HorarioDisponivel(boxId, inicio)>`. Quem busca os dados é um service de outra camada.
- Regra de conflito entre intervalos semiabertos `[início, fim)`: `fimA > inicioB && fimB > inicioA`. Encostar não é conflito.
- Para cada box: filtra agendamentos do box, do dia (no fuso do estabelecimento) e com status que ocupam (`AGENDADO`, `EM_USO`, lista explícita); testa candidatos de passo em passo enquanto `t + duração <= fechamento`.

## Consequências
- Testável com JUnit puro, com cenários escritos à mão.
- O status em lista explícita faz um status novo não bloquear o box sem decisão consciente.
- Pendências: não oferecer horários passados quando o dia é hoje (injetar `Clock`); expediente por dia da semana; passo configurável.

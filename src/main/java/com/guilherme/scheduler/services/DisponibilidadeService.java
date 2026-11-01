package com.guilherme.scheduler.services;

import com.guilherme.scheduler.enums.AgendamentoStatus;
import com.guilherme.scheduler.record.Agendamento;
import com.guilherme.scheduler.record.Configuracao;
import com.guilherme.scheduler.record.HorarioDisponivel;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class DisponibilidadeService {

    public List<HorarioDisponivel> encontrarHorario(
            LocalDate dia, Duration duracaoServico, List<Agendamento> agendamentos, List<Long> boxIds,
            Configuracao configuracao) {

        LocalTime abertura = configuracao.abertura();
        LocalTime fechamento = configuracao.fechamento();

        Duration passo = configuracao.passo();

        var bloqueiam = List.of(AgendamentoStatus.AGENDADO, AgendamentoStatus.EM_USO);

        List<HorarioDisponivel> horariosDisponiveis = new ArrayList<>();
        ZonedDateTime horarioFechamento = ZonedDateTime.of(dia, fechamento, configuracao.fuso());
        for (var box : boxIds) {
            List<Agendamento> agendamentosDoBox = agendamentos.stream().filter(ag -> ag.boxId().equals(box)
                    && ag.inicio().withZoneSameInstant(configuracao.fuso()).toLocalDate().isEqual(dia) && bloqueiam.contains(ag.status())).toList();

            ZonedDateTime t = ZonedDateTime.of(dia, abertura, configuracao.fuso());

            while (!t.plus(duracaoServico).isAfter(horarioFechamento)){
                boolean conflita = false;
                ZonedDateTime fimCandidato = t.plus(duracaoServico);
                for (var ag : agendamentosDoBox) {

                    if(ag.fim().isAfter(t) && fimCandidato.isAfter(ag.inicio())){
                        conflita = true;
                        break;
                    }
                }
                if(!conflita){
                    horariosDisponiveis.add(new HorarioDisponivel(box, t));
                }
                t = t.plus(passo);
            }

        }

        return horariosDisponiveis;
    }
}

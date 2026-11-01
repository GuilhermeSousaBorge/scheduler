package com.guilherme.scheduler;

import com.guilherme.scheduler.enums.AgendamentoStatus;
import com.guilherme.scheduler.record.Agendamento;
import com.guilherme.scheduler.record.Configuracao;
import com.guilherme.scheduler.record.HorarioDisponivel;
import com.guilherme.scheduler.services.DisponibilidadeService;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DisponibilidadeServiceTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate QUARTA = LocalDate.of(2026, 10, 28);
    private static final LocalDate QUINTA = LocalDate.of(2026, 10, 29);

    private final DisponibilidadeService service = new DisponibilidadeService();

    @Test
    void boxSemAgendamentosOfereceTodosOsHorariosQueCabemNoExpediente() {
        // Arrange
        var configuracao = new Configuracao(FUSO, LocalTime.of(8, 0), LocalTime.of(10, 0), Duration.ofMinutes(30));
        List<Agendamento> agendamentos = List.of();
        List<Long> boxIds = List.of(1L);

        // Act
        var resultado = service.encontrarHorario(
                QUARTA, Duration.ofHours(1), agendamentos, boxIds, configuracao);

        // Assert
        var esperado = List.of(
                new HorarioDisponivel(1L, em(8, 0, QUARTA)),
                new HorarioDisponivel(1L, em(8, 30, QUARTA)),
                new HorarioDisponivel(1L, em(9, 0, QUARTA))
        );
        assertEquals(esperado, resultado);
    }

    @Test
    void boxComAgendamentosCanceladosEmAndamento(){
        //Arrange
        var configuracao = new Configuracao(FUSO, LocalTime.of(8, 0), LocalTime.of(12, 0), Duration.ofMinutes(30));
        List<Agendamento> agendamentos = List.of(
                new Agendamento(1L, em(9, 0, QUINTA), em(10, 0, QUINTA), AgendamentoStatus.AGENDADO),
                new Agendamento(1L, em(10, 30, QUINTA), em(11, 30, QUINTA), AgendamentoStatus.EM_USO),

                new Agendamento(2L, em(8, 0, QUINTA), em(9, 0, QUINTA), AgendamentoStatus.EM_USO),
                new Agendamento(2L, em(9, 0, QUINTA), em(10, 0, QUINTA), AgendamentoStatus.AGENDADO)

        );
        List<Long> boxIds = List.of(1L, 2L, 3L);

        //Act
        var resultado = service.encontrarHorario(
                QUINTA, Duration.ofHours(1), agendamentos, boxIds, configuracao
        );
        //Assert
        var esperado = List.of(
                new HorarioDisponivel(1L, em(8, 0, QUINTA)),
                new HorarioDisponivel(2L, em(10, 0, QUINTA)),
                new HorarioDisponivel(2L, em(10, 30, QUINTA)),
                new HorarioDisponivel(2L, em(11, 0, QUINTA)),
                new HorarioDisponivel(3L, em(8, 0, QUINTA)),
                new HorarioDisponivel(3L, em(8, 30, QUINTA)),
                new HorarioDisponivel(3L, em(9, 0, QUINTA)),
                new HorarioDisponivel(3L, em(9, 30, QUINTA)),
                new HorarioDisponivel(3L, em(10, 0, QUINTA)),
                new HorarioDisponivel(3L, em(10, 30, QUINTA)),
                new HorarioDisponivel(3L, em(11, 0, QUINTA))
        );
        assertEquals(esperado, resultado);
    }

    /**
     * @param hora
     * @param minuto
     * @return
     */
    private ZonedDateTime em(int hora, int minuto, LocalDate data) {
        return ZonedDateTime.of(data, LocalTime.of(hora, minuto), FUSO);
    }
}

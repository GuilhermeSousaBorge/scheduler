package com.guilherme.scheduler.record;

import com.guilherme.scheduler.SchedulerApplication;
import com.guilherme.scheduler.enums.AgendamentoStatus;

import java.time.ZonedDateTime;

public record Agendamento(Long boxId, ZonedDateTime inicio, ZonedDateTime fim, AgendamentoStatus status){}

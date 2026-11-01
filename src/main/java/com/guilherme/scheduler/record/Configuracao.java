package com.guilherme.scheduler.record;

import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;

public record Configuracao(ZoneId fuso, LocalTime abertura, LocalTime fechamento, Duration passo){}
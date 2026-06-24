package edu.rutmiit.demo.footballscoreapicontract.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статус футбольного матча")
public enum MatchStatus {
    SCHEDULED("Запланирован"),
    LIVE("В прямом эфире"),
    HALFTIME("Перерыв"),
    FINISHED("Завершен"),
    CANCELLED("Отменен");

    private final String description;

    MatchStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
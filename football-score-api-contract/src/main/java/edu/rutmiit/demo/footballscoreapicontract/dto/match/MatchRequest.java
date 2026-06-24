package edu.rutmiit.demo.footballscoreapicontract.dto.match;

import edu.rutmiit.demo.footballscoreapicontract.validation.ValidTeams;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * DTO для создания новой книги.
 */
@Schema(description = "Запрос на создание матча")
@ValidTeams
public record MatchRequest(

        @Schema(description = "ID домашней команды", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID домашней команды не может быть пустым")
        Long homeTeamId,

        @Schema(description = "ID гостевой команды", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "ID гостевой команды не может быть пустым")
        Long awayTeamId

) {}
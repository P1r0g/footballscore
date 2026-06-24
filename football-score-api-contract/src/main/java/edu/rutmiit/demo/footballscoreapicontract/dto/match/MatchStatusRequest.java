package edu.rutmiit.demo.footballscoreapicontract.dto.match;

import edu.rutmiit.demo.footballscoreapicontract.dto.MatchStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на изменение статуса матча")
public record MatchStatusRequest(
        @Schema(description = "Статус матча", example = "LIVE", requiredMode = Schema.RequiredMode.REQUIRED)
        MatchStatus status
) {}

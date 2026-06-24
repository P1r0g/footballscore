package edu.rutmiit.demo.footballscoreapicontract.dto.match;

import io.swagger.v3.oas.annotations.media.Schema;

public record GoalRequest(
        @Schema(description = "Id команды, забившей гол", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long teamId
) {
}

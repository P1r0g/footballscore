package edu.rutmiit.demo.footballscoreapicontract.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Запрос для частичного обновления автора (PATCH, семантика JSON Merge Patch).
 *
 * Все поля необязательны. Передайте только то, что нужно изменить.
 * Поля, которые не переданы (null), сервис оставляет без изменений.
 */
@Schema(description = "Частичное обновление команды (PATCH). Передайте только те поля, которые нужно изменить.")
public record PatchTeamRequest(

        @Schema(description = "Название команды", example = "Реал Мадрид", requiredMode = Schema.RequiredMode.REQUIRED)
        @Size(max = 100, message = "Название команды не может превышать 100 символов")
        String name,

        @Schema(description = "Страна команды", example = "Испания", requiredMode = Schema.RequiredMode.REQUIRED)
        @Size(max = 100, message = "Страна не может превышать 100 символов")
        String country,

        @Schema(description = "ФИО тренера команды", example = "Жозе Моуриньо")
        @Size(max = 255, message = "ФИО тренера не может превышать 255 символов")
        String coach
) {}

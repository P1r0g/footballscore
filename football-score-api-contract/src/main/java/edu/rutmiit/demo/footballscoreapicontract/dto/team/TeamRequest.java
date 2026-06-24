package edu.rutmiit.demo.footballscoreapicontract.dto.team;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/**
 * DTO для создания или полного обновления автора (POST / PUT).
 * Все обязательные поля должны присутствовать.
 */
@Schema(description = "Запрос на создание или полное обновление команды")
public record TeamRequest(

        @Schema(description = "Название команды", example = "Реал Мадрид", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Название команды не может быть пустым")
        @Size(max = 100, message = "Название команды не может превышать 100 символов")
        String name,

        @Schema(description = "Страна команды", example = "Испания", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Страна команды не может быть пустой")
        @Size(max = 100, message = "Страна не может превышать 100 символов")
        String country,

        @Schema(description = "ФИО тренера команды", example = "Жозе Моуриньо")
        @NotBlank(message = "Тренер команды должен существовать")
        @Size(max = 255, message = "ФИО тренера не может превышать 255 символов")
        String coach
) {}
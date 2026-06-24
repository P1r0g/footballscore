package edu.rutmiit.demo.footballscoreapicontract.dto.team;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

/**
 * Данные автора в ответе API.
 *
 * Расширяет RepresentationModel для поддержки HATEOAS-ссылок — поэтому здесь
 * обычный класс с Lombok, а не record (record не может расширять классы).
 * Поля со значением null не попадают в JSON ответа.
 */
@Getter
@Builder
    @EqualsAndHashCode(callSuper = false) // не включаем HATEOAS-ссылки в сравнение equals
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "teams", itemRelation = "team")
@Schema(description = "Информация об команде")
public class TeamResponse extends RepresentationModel<TeamResponse> {

    @Schema(description = "Уникальный идентификатор команды", example = "1")
    private final Long id;

    @Schema(description = "Название команды", example = "Реал Мадрид")
    private final String name;

    @Schema(description = "Страна команды", example = "Испания")
    private final String country;

    @Schema(description = "ФИО тренера команды", example = "Жозе Моуриньо")
    private final String coach;

}
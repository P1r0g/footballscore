package edu.rutmiit.demo.footballscoreapicontract.dto.match;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.rutmiit.demo.footballscoreapicontract.dto.MatchStatus;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

/**
 * Данные книги в ответе API.
 *
 * Расширяет RepresentationModel для поддержки HATEOAS-ссылок — поэтому здесь
 * обычный класс с Lombok, а не record.
 * Поля со значением null не попадают в JSON ответа.
 */
@Getter
@Builder
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Relation(collectionRelation = "matches", itemRelation = "match")
@Schema(description = "Информация о матче")
public class MatchResponse extends RepresentationModel<MatchResponse> {

    @Schema(description = "Уникальный идентификатор матча", example = "1")
    private final Long id;

    @Schema(description = "Домашняя команда")
    private final TeamResponse homeTeam;

    @Schema(description = "Гостевая команда")
    private final TeamResponse awayTeam;

    @Schema(description = "Счет домашней команды")
    private final Short homeScore;

    @Schema(description = "Счет гостевой команды")
    private final Short awayScore;

    @Schema(description = "Статус матча")
    private final MatchStatus status;
}

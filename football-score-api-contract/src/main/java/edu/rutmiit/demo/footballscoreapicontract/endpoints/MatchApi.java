package edu.rutmiit.demo.footballscoreapicontract.endpoints;

import edu.rutmiit.demo.footballscoreapicontract.config.FootballScoreApiContractConfig;
import edu.rutmiit.demo.footballscoreapicontract.dto.*;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.GoalRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchStatusRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контракт API для управления матчами.
 * Реализующий контроллер в сервисе должен имплементировать этот интерфейс.
 */
@Tag(name = "Matches", description = "Управление матчами")
@RequestMapping(
        value = "/api/matches",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public interface MatchApi {

    @Operation(
            summary = "Получить матч по ID",
            security = @SecurityRequirement(name = FootballScoreApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "200", description = "Матч найден")
    @ApiResponse(responseCode = "404", description = "Матч не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    EntityModel<MatchResponse> getMatchById(
            @Parameter(description = "ID матча", required = true, example = "1") @PathVariable Long id
    );

    @Operation(
            summary = "Список матчей",
            description = """
                    Возвращает постраничный список матчей с HATEOAS-ссылками.
                    Поддерживает комбинирование фильтров: authorId, genre, publishedYear и titleSearch
                    можно передавать одновременно.
                    """,
            security = @SecurityRequirement(name = FootballScoreApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "200", description = "Постраничный список книг")
    @GetMapping
    PagedModel<EntityModel<MatchResponse>> getAllMatches(
            @Parameter(description = "Фильтр по ID команды") @RequestParam(required = false) Long teamId,
            @Parameter(description = "Фильтр по статусу") @RequestParam(required = false) MatchStatus status,
            @Parameter(description = "Номер страницы (0..N)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size
    );

    @Operation(
            summary = "Создать матч",
            security = @SecurityRequirement(name = FootballScoreApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "201", description = "Матч создан. Location header содержит URI нового ресурса.")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Команда с указанным id не найдена",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<EntityModel<MatchResponse>> createMatch(@Valid @RequestBody MatchRequest request);

//    @Operation(
//            summary = "Полное обновление книги (PUT)",
//            description = "Заменяет все поля книги. Автора изменить нельзя. "
//                    + "Для обновления отдельных полей используйте PATCH.",
//            security = @SecurityRequirement(name = FootballScoreApiContractConfig.SECURITY_SCHEME_BEARER)
//    )
//    @ApiResponse(responseCode = "200", description = "Книга обновлена")
//    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
//            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
//    @ApiResponse(responseCode = "404", description = "Книга не найдена",
//            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
//    @ApiResponse(responseCode = "409", description = "Книга с таким ISBN уже существует",
//            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
//    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
//    EntityModel<MatchResponse> updateBook(
//            @Parameter(description = "ID книги", required = true, example = "1") @PathVariable Long id,
//            @Valid @RequestBody UpdateBookRequest request
//    );
//
//    @Operation(
//            summary = "Частичное обновление книги (PATCH)",
//            description = """
//                    Обновляет только переданные поля (семантика JSON Merge Patch, RFC 7396).
//                    Непереданные поля остаются без изменений. Автора книги изменить нельзя.
//                    """,
//            security = @SecurityRequirement(name = FootballScoreApiContractConfig.SECURITY_SCHEME_BEARER)
//    )
//    @ApiResponse(responseCode = "200", description = "Книга обновлена")
//    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
//            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
//    @ApiResponse(responseCode = "404", description = "Книга не найдена",
//            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
//    @ApiResponse(responseCode = "409", description = "Книга с таким ISBN уже существует",
//            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
//    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
//    EntityModel<MatchResponse> patchBook(
//            @Parameter(description = "ID книги", required = true, example = "1") @PathVariable Long id,
//            @Valid @RequestBody PatchMatchRequest request
//    );

    @Operation(
            summary = "Удалить матч",
            security = @SecurityRequirement(name = FootballScoreApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "204", description = "Матч удален")
    @ApiResponse(responseCode = "404", description = "Матч не найден",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteMatch(
            @Parameter(description = "ID матча", required = true, example = "1") @PathVariable Long id
    );

    @Operation(
            summary = "Изменить статус матча",
            description = """
                Изменяет статус матча.

                Допустимые переходы:
                SCHEDULED -> LIVE
                SCHEDULED -> CANCELLED
                LIVE -> HALFTIME
                HALFTIME -> LIVE
                LIVE -> FINISHED
                """,
            security = @SecurityRequirement(
                    name = FootballScoreApiContractConfig.SECURITY_SCHEME_BEARER
            )
    )
    @ApiResponse(responseCode = "200", description = "Статус матча изменён")
    @ApiResponse(responseCode = "400", description = "Некорректный переход статуса",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
    @ApiResponse(responseCode = "404", description = "Матч не найден",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
    @PatchMapping(
            value = "/{id}/status",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    EntityModel<MatchResponse> updateMatchStatus(
            @Parameter(description = "ID матча", required = true, example = "1")
            @PathVariable Long id,

            @Valid
            @RequestBody MatchStatusRequest request
    );

    @Operation(
            summary = "Засчитать гол",
            description = """
                Увеличивает счёт одной из команд на 1.

                Гол можно засчитать только для команды,
                участвующей в данном матче.

                Голы разрешены только когда матч находится
                в статусе LIVE.
                """,
            security = @SecurityRequirement(
                    name = FootballScoreApiContractConfig.SECURITY_SCHEME_BEARER
            )
    )
    @ApiResponse(responseCode = "200", description = "Гол успешно засчитан")
    @ApiResponse(responseCode = "400", description = "Команда не участвует в матче или матч не находится в статусе LIVE",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
    @ApiResponse(responseCode = "404", description = "Матч не найден",
            content = @Content(
                    schema = @Schema(implementation = ErrorResponse.class)
            ))
    @PatchMapping(
            value = "/{id}/goal",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    EntityModel<MatchResponse> addGoal(
            @Parameter(
                    description = "ID матча",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id,

            @Valid
            @RequestBody GoalRequest request
    );
}

  
package edu.rutmiit.demo.footballscoreapicontract.endpoints;

import edu.rutmiit.demo.footballscoreapicontract.config.FootballScoreApiContractConfig;
import edu.rutmiit.demo.footballscoreapicontract.dto.*;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.PatchTeamRequest;
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
 * Контракт API для управления командами.
 * Реализующий контроллер в сервисе должен имплементировать этот интерфейс.
 */
@Tag(name = "Authors", description = "Управление командами")
@RequestMapping(
        value = "/api/teams",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public interface TeamApi {

    @Operation(
            summary = "Список команд",
            description = "Возвращает постраничный список команд с HATEOAS-ссылками. "
                    + "Ссылки prev/next позволяют клиенту навигировать по страницам без знания офсетов.",
            security = @SecurityRequirement(name = FootballScoreApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "200", description = "Список команд")
    @GetMapping
    PagedModel<EntityModel<TeamResponse>> getAllTeams(
            @Parameter(description = "Номер страницы (0..N)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы", example = "20")
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(
            summary = "Получить команду по ID",
            security = @SecurityRequirement(name = FootballScoreApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "200", description = "Команда найдена")
    @ApiResponse(responseCode = "404", description = "Команда не найдена",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    EntityModel<TeamResponse> getTeamById(
            @Parameter(description = "ID команды", required = true, example = "1") @PathVariable Long id
    );

    @Operation(
            summary = "Создать команду",
            security = @SecurityRequirement(name = FootballScoreApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "201", description = "Команда создана. Location header содержит URI нового ресурса.")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<EntityModel<TeamResponse>> createTeam(@Valid @RequestBody TeamRequest request);

    @Operation(
            summary = "Полное обновление команды (PUT)",
            description = "Заменяет все поля команды. Для обновления отдельных полей используйте PATCH.",
            security = @SecurityRequirement(name = FootballScoreApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "200", description = "Команда обновлёна")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Команда не найдена",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    EntityModel<TeamResponse> updateTeam(
            @Parameter(description = "ID команды", required = true, example = "1") @PathVariable Long id,
            @Valid @RequestBody TeamRequest request
    );

    @Operation(
            summary = "Частичное обновление команды (PATCH)",
            description = """
                    Обновляет только переданные поля (семантика JSON Merge Patch, RFC 7396).
                    Непереданные поля остаются без изменений.
                    """,
            security = @SecurityRequirement(name = FootballScoreApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "200", description = "Команда обновлена")
    @ApiResponse(responseCode = "400", description = "Ошибка валидации",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Команда не найдена",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    EntityModel<TeamResponse> patchTeam(
            @Parameter(description = "ID команды", required = true, example = "1") @PathVariable Long id,
            @Valid @RequestBody PatchTeamRequest request
    );

    @Operation(
            summary = "Удалить команду",
            description = "Удаляет команду и все его матчи (каскадное удаление).",
            security = @SecurityRequirement(name = FootballScoreApiContractConfig.SECURITY_SCHEME_BEARER)
    )
    @ApiResponse(responseCode = "204", description = "Команда удалена")
    @ApiResponse(responseCode = "404", description = "Команда не найдена",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteTeam(
            @Parameter(description = "ID команды", required = true, example = "1") @PathVariable Long id
    );

//    @Operation(
//            summary = "Матчи команды (суб-ресурс)",
//            description = """
//                    Возвращает постраничный список матчей указанной команды.
//                    Это суб-ресурс (концепция REST): /teams/{id}/matches.
//                    Эквивалентен GET /matches?teamId={id}, но точнее отражает иерархию.
//                    """,
//            security = @SecurityRequirement(name = FootballScoreApiContractConfig.SECURITY_SCHEME_BEARER)
//    )
//    @ApiResponse(responseCode = "200", description = "Список матчей команды")
//    @ApiResponse(responseCode = "404", description = "Команда не найдена",
//            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
//    @GetMapping("/{id}/books")
//    PagedModel<EntityModel<MatchResponse>> getMatchesByTeam(
//            @Parameter(description = "ID команды", required = true, example = "1") @PathVariable Long id,
//            @Parameter(description = "Номер страницы (0..N)", example = "0") @RequestParam(defaultValue = "0") int page,
//            @Parameter(description = "Размер страницы", example = "20") @RequestParam(defaultValue = "20") int size
//    );
}

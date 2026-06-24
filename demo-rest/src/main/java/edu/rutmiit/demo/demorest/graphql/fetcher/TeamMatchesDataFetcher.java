package edu.rutmiit.demo.demorest.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.InputArgument;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.PagedResponse;
import edu.rutmiit.demo.demorest.graphql.types.MatchConnectionGql;
import edu.rutmiit.demo.demorest.graphql.types.PageInfoGql;
import edu.rutmiit.demo.demorest.service.MatchService;

/**
 * Вложенный резолвер для поля Team.matches.
 *
 * Срабатывает когда клиент запрашивает матчи команды:
 *
 *   query {
 *     team(id: "1") {
 *       name
 *       country
 *       coach
 *       matches(page: 0, size: 5) {    ← этот резолвер
 *         content {
 *           title
 *         }
 *       }
 *     }
 *   }
 *
 * Демонстрирует работу с аргументами вложенного поля (page, size)
 * и доступ к родительскому объекту (Team).
 */
@DgsComponent
public class TeamMatchesDataFetcher {

    private final MatchService matchService;

    public TeamMatchesDataFetcher(MatchService matchService) {
        this.matchService = matchService;
    }

    /**
     * Загружает матчи указанной команды с пагинацией.
     *
     * Аргументы (page, size) берутся из GraphQL-запроса через @InputArgument.
     * Родительский объект (Team) берётся из DgsDataFetchingEnvironment.
     */
    @DgsData(parentType = "Team", field = "matches")
    public MatchConnectionGql matches(
            DgsDataFetchingEnvironment dfe,
            @InputArgument Integer page,
            @InputArgument Integer size) {

        TeamResponse author = dfe.getSource();

        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        // Фильтруем книги по ID автора — переиспользуем сервис
        PagedResponse<MatchResponse> paged = matchService.findAllMatches(
                author.getId(), null,  pageNum, pageSize);

        return new MatchConnectionGql(
                paged.content(),
                new PageInfoGql(paged.pageNumber(), paged.pageSize(), paged.totalPages(), paged.last()),
                (int) paged.totalElements());
    }
}

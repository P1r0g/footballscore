package edu.rutmiit.demo.demorest.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchResponse;
import edu.rutmiit.demo.demorest.service.TeamService;

/**
 * Вложенный резолвер для поля Match.team.
 *
 * В GraphQL каждое поле может иметь свой резолвер. Когда клиент запрашивает
 * книгу вместе с автором:
 *
 *   query {
 *     match(id: "1") {
 *       title
 *       homeTeam {       ← этот резолвер срабатывает
 *         name
 *       }
 *       awayTeam {
 *           name
 *       }
 *     }
 *   }
 *
 * DGS вызывает этот метод для каждой игры, передавая родительский объект
 * через DgsDataFetchingEnvironment. Если клиент НЕ запросил поле team,
 * этот резолвер вообще не вызывается — экономия ресурсов.
 *
 * Аннотация @DgsData(parentType, field) привязывает метод к конкретному полю
 * конкретного типа в GraphQL-схеме.
 */
@DgsComponent
public class MatchTeamDataFetcher {

    private final TeamService teamService;

    public MatchTeamDataFetcher(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * Загружает автора для заданной книги.
     *
     * Родительский объект (Match) извлекается из DgsDataFetchingEnvironment.
     * В нашем in-memory хранилище автор уже вложен в MatchResponse,
     * поэтому мы просто его возвращаем. В реальном проекте здесь был бы
     * вызов к базе данных или внешнему сервису.
     */
    @DgsData(parentType = "Match", field = "homeTeam")
    public TeamResponse homeTeam(DgsDataFetchingEnvironment dfe) {
        MatchResponse match = dfe.getSource();
        return match.getHomeTeam();
    }

    @DgsData(parentType = "Match", field = "awayTeam")
    public TeamResponse awayTeam(DgsDataFetchingEnvironment dfe) {
        MatchResponse match = dfe.getSource();
        return match.getAwayTeam();
    }
}

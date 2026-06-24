package edu.rutmiit.demo.demorest.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.PagedResponse;
import edu.rutmiit.demo.demorest.graphql.types.TeamConnectionGql;
import edu.rutmiit.demo.demorest.graphql.types.CreateTeamInputGql;
import edu.rutmiit.demo.demorest.graphql.types.PageInfoGql;
import edu.rutmiit.demo.demorest.graphql.types.UpdateTeamInputGql;
import edu.rutmiit.demo.demorest.service.TeamService;

/**
 * DataFetcher для операций с авторами.
 *
 * Обрабатывает корневые поля Query и Mutation, связанные с авторами.
 * Вложенные поля (Author.books) обрабатываются в AuthorBooksDataFetcher.
 *
 * Принцип разделения: один DataFetcher — одна группа связанных операций.
 * Это делает код более читаемым и тестируемым.
 */
@DgsComponent
public class TeamDataFetcher {

    private final TeamService teamService;

    public TeamDataFetcher(TeamService teamService) {
        this.teamService = teamService;
    }

    /**
     * Получение команды по идентификатору.
     * Соответствует полю Query.team(id: ID!) в схеме.
     */
    @DgsQuery
    public TeamResponse team(@InputArgument String id) {
        return teamService.findById(Long.parseLong(id));
    }

    /**
     * Список команд с пагинацией.
     * Соответствует полю Query.teams(page, size) в схеме.
     */
    @DgsQuery
    public TeamConnectionGql teams(
            @InputArgument Integer page,
            @InputArgument Integer size) {

        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        PagedResponse<TeamResponse> paged = teamService.findAll(pageNum, pageSize);

        return new TeamConnectionGql(
                paged.content(),
                new PageInfoGql(paged.pageNumber(), paged.pageSize(), paged.totalPages(), paged.last()),
                (int) paged.totalElements());
    }

    /**
     * Создание команды.
     * Соответствует полю Mutation.createTeam(input) в схеме.
     */
    @DgsMutation
    public TeamResponse createTeam(@InputArgument CreateTeamInputGql input) {
        TeamRequest request = new TeamRequest(
                input.name(),
                input.country(),
                input.coach()
        );
        return teamService.create(request);
    }

    /**
     * Обновление команды.
     * Соответствует полю Mutation.updateTeam(id, input) в схеме.
     */
    @DgsMutation
    public TeamResponse updateTeam(@InputArgument String id, @InputArgument UpdateTeamInputGql input) {
        TeamRequest request = new TeamRequest(
                input.name(),
                input.country(),
                input.coach()
        );
        return teamService.update(Long.parseLong(id), request);
    }

    /**
     * Удаление команды и всех ее матчей (каскадно).
     * Соответствует полю Mutation.deleteTeam(id) в схеме.
     */
    @DgsMutation
    public boolean deleteTeam(@InputArgument String id) {
        teamService.delete(Long.parseLong(id));
        return true;
    }
}

package edu.rutmiit.demo.demorest.graphql.fetcher;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import edu.rutmiit.demo.demorest.graphql.types.*;
import edu.rutmiit.demo.footballscoreapicontract.dto.MatchStatus;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.GoalRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.PagedResponse;
import edu.rutmiit.demo.demorest.service.MatchService;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchStatusRequest;

/**
 * DataFetcher для операций с книгами.
 *
 * Аннотация @DgsComponent регистрирует этот класс как компонент DGS-фреймворка.
 * Каждый метод с @DgsQuery или @DgsMutation привязывается к соответствующему полю
 * в GraphQL-схеме. DGS находит их по имени метода (или по явному параметру field).
 *
 * Этот DataFetcher обрабатывает корневые поля Query и Mutation для книг.
 * Вложенные поля (Match.team) обрабатываются в отдельном резолвере.
 */
@DgsComponent
public class MatchDataFetcher {

    private final MatchService matchService;

    public MatchDataFetcher(MatchService matchService) {
        this.matchService = matchService;
    }

    @DgsQuery
    public MatchResponse match(@InputArgument String id) {
        return matchService.findMatchById(Long.parseLong(id));
    }

    @DgsQuery
    public MatchConnectionGql matches(
            @InputArgument MatchFilterGql filter,
            @InputArgument Integer page,
            @InputArgument Integer size) {

        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;

        Long teamId = null;
        MatchStatus status = null;

        if (filter != null) {
            teamId = filter.teamId() != null
                    ? Long.parseLong(filter.teamId())
                    : null;

            status = filter.status();
        }

        PagedResponse<MatchResponse> paged =
                matchService.findAllMatches(
                        teamId,
                        status,
                        pageNum,
                        pageSize
                );

        return new MatchConnectionGql(
                paged.content(),
                new PageInfoGql(
                        paged.pageNumber(),
                        paged.pageSize(),
                        paged.totalPages(),
                        paged.last()
                ),
                (int) paged.totalElements()
        );
    }

    @DgsMutation
    public MatchResponse createMatch(
            @InputArgument CreateMatchInputGql input) {

        MatchRequest request = new MatchRequest(
                Long.parseLong(input.homeTeamId()),
                Long.parseLong(input.awayTeamId())
        );

        return matchService.createMatch(request);
    }

    @DgsMutation
    public boolean deleteMatch(
            @InputArgument String id) {

        matchService.deleteMatch(Long.parseLong(id));
        return true;
    }

    @DgsMutation
    public MatchResponse addGoal(
            @InputArgument String matchId,
            @InputArgument GoalInputGql input) {

        return matchService.addGoal(
                Long.parseLong(matchId),
                new GoalRequest(Long.parseLong(input.teamId()))
        );
    }

    @DgsMutation
    public MatchResponse updateMatchStatus(
            @InputArgument String matchId,
            @InputArgument UpdateMatchStatusInputGql input) {

        return matchService.updateMatchStatus(
                Long.parseLong(matchId),
                new MatchStatusRequest(input.status())
        );
    }
}

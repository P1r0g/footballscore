package edu.rutmiit.demo.demorest.controllers;

import edu.rutmiit.demo.footballscoreapicontract.dto.*;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.PatchTeamRequest;
import edu.rutmiit.demo.footballscoreapicontract.endpoints.TeamApi;
import edu.rutmiit.demo.demorest.assemblers.TeamModelAssembler;
import edu.rutmiit.demo.demorest.assemblers.MatchModelAssembler;
import edu.rutmiit.demo.demorest.service.TeamService;
import edu.rutmiit.demo.demorest.service.MatchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TeamController implements TeamApi {

    private final TeamService teamService;
    private final MatchService matchService;
    private final TeamModelAssembler teamModelAssembler;
    private final MatchModelAssembler matchModelAssembler;
    private final PagedResourcesAssembler<TeamResponse> pagedAuthorsAssembler;
    private final PagedResourcesAssembler<MatchResponse> pagedBooksAssembler;

    public TeamController(TeamService teamService,
                          MatchService matchService,
                          TeamModelAssembler teamModelAssembler,
                          MatchModelAssembler matchModelAssembler,
                          PagedResourcesAssembler<TeamResponse> pagedAuthorsAssembler,
                          PagedResourcesAssembler<MatchResponse> pagedBooksAssembler) {
        this.teamService = teamService;
        this.matchService = matchService;
        this.teamModelAssembler = teamModelAssembler;
        this.matchModelAssembler = matchModelAssembler;
        this.pagedAuthorsAssembler = pagedAuthorsAssembler;
        this.pagedBooksAssembler = pagedBooksAssembler;
    }

    @Override
    public PagedModel<EntityModel<TeamResponse>> getAllTeams(int page, int size) {
        PagedResponse<TeamResponse> paged = teamService.findAll(page, size);
        Page<TeamResponse> springPage = new PageImpl<>(
                paged.content(),
                PageRequest.of(paged.pageNumber(), paged.pageSize()),
                paged.totalElements()
        );
        return pagedAuthorsAssembler.toModel(springPage, teamModelAssembler);
    }

    @Override
    public EntityModel<TeamResponse> getTeamById(Long id) {
        return teamModelAssembler.toModel(teamService.findById(id));
    }

    @Override
    public ResponseEntity<EntityModel<TeamResponse>> createTeam(TeamRequest request) {
        TeamResponse created = teamService.create(request);
        EntityModel<TeamResponse> model = teamModelAssembler.toModel(created);
        return ResponseEntity
                .created(model.getRequiredLink("self").toUri())
                .body(model);
    }

    @Override
    public EntityModel<TeamResponse> updateTeam(Long id, TeamRequest request) {
        return teamModelAssembler.toModel(teamService.update(id, request));
    }

    @Override
    public EntityModel<TeamResponse> patchTeam(Long id, PatchTeamRequest request) {
        return teamModelAssembler.toModel(teamService.patchTeam(id, request));
    }

    @Override
    public void deleteTeam(Long id) {
        teamService.delete(id);
    }

//    @Override
//    public PagedModel<EntityModel<MatchResponse>> getMatchesByTeam(Long id, int page, int size) {
//        // Проверяем что автор существует (выбросит 404 если нет)
//        teamService.findById(id);
//        PagedResponse<MatchResponse> paged = matchService.findAllMatches(id, null, null, page, size);
//        Page<MatchResponse> springPage = new PageImpl<>(
//                paged.content(),
//                PageRequest.of(paged.pageNumber(), paged.pageSize()),
//                paged.totalElements()
//        );
//        return pagedBooksAssembler.toModel(springPage, matchModelAssembler);
//    }
}

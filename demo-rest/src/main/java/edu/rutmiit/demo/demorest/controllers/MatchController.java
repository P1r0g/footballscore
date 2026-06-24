package edu.rutmiit.demo.demorest.controllers;

import edu.rutmiit.demo.demorest.assemblers.MatchModelAssembler;
import edu.rutmiit.demo.demorest.service.MatchService;
import edu.rutmiit.demo.footballscoreapicontract.dto.MatchStatus;
import edu.rutmiit.demo.footballscoreapicontract.dto.PagedResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.GoalRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchStatusRequest;
import edu.rutmiit.demo.footballscoreapicontract.endpoints.MatchApi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MatchController implements MatchApi {

    private final MatchService matchService;
    private final MatchModelAssembler matchModelAssembler;
    private final PagedResourcesAssembler<MatchResponse> pagedResourcesAssembler;

    public MatchController(MatchService matchService, MatchModelAssembler matchModelAssembler,
                           PagedResourcesAssembler<MatchResponse> pagedResourcesAssembler) {
        this.matchService = matchService;
        this.matchModelAssembler = matchModelAssembler;
        this.pagedResourcesAssembler = pagedResourcesAssembler;
    }

    @Override
    public EntityModel<MatchResponse> getMatchById(Long id) {
        return matchModelAssembler.toModel(matchService.findMatchById(id));
    }

    @Override
    public PagedModel<EntityModel<MatchResponse>> getAllMatches(Long teamId, MatchStatus status, int page, int size) {
        PagedResponse<MatchResponse> paged = matchService.findAllMatches(teamId, status, page, size);
        Page<MatchResponse> springPage = new PageImpl<>(
                paged.content(),
                PageRequest.of(paged.pageNumber(), paged.pageSize()),
                paged.totalElements()
        );
        return pagedResourcesAssembler.toModel(springPage, matchModelAssembler);
    }

    @Override
    public ResponseEntity<EntityModel<MatchResponse>> createMatch(MatchRequest request) {
        MatchResponse created = matchService.createMatch(request);
        EntityModel<MatchResponse> model = matchModelAssembler.toModel(created);
        return ResponseEntity
                .created(model.getRequiredLink("self").toUri())
                .body(model);
    }

//    @Override
//    public EntityModel<MatchResponse> updateBook(Long id, UpdateBookRequest request) {
//        return matchModelAssembler.toModel(bookService.updateBook(id, request));
//    }
//
//    @Override
//    public EntityModel<MatchResponse> patchBook(Long id, PatchMatchRequest request) {
//        return matchModelAssembler.toModel(bookService.patchBook(id, request));
//    }

    @Override
    public void deleteMatch(Long id) {
        matchService.deleteMatch(id);
    }

    @Override
    public EntityModel<MatchResponse> updateMatchStatus(Long id, MatchStatusRequest request) {
        return matchModelAssembler.toModel(matchService.updateMatchStatus(id, request));
    }

    @Override
    public EntityModel<MatchResponse> addGoal(Long id, GoalRequest request) {
        return matchModelAssembler.toModel(matchService.addGoal(id, request));
    }
}
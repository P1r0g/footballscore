package edu.rutmiit.demo.demorest.service;

import edu.rutmiit.demo.demorest.entity.TeamEntity;
import edu.rutmiit.demo.demorest.event.TeamEventPublisher;
import edu.rutmiit.demo.demorest.repository.TeamRepository;
import edu.rutmiit.demo.demorest.storage.InMemoryStorage;
import edu.rutmiit.demo.footballscoreapicontract.dto.PagedResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.PatchTeamRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamResponse;
import edu.rutmiit.demo.footballscoreapicontract.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final InMemoryStorage storage;
    private final TeamEventPublisher eventPublisher;

    public TeamService(
            TeamRepository teamRepository,
            InMemoryStorage storage,
            TeamEventPublisher eventPublisher
    ) {
        this.teamRepository = teamRepository;
        this.storage = storage;
        this.eventPublisher = eventPublisher;
    }

    public PagedResponse<TeamResponse> findAll(int page, int size) {
        Page<TeamEntity> result = teamRepository.findAll(
                PageRequest.of(page, size, Sort.by("id"))
        );

        List<TeamResponse> content = result.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PagedResponse<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    public TeamResponse findById(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public TeamResponse create(TeamRequest request) {
        TeamEntity entity = new TeamEntity();
        entity.setName(request.name());
        entity.setCountry(request.country());
        entity.setCoach(request.coach());

        TeamResponse response = toResponse(
                teamRepository.save(entity)
        );

        afterCommit(() -> eventPublisher.publishCreated(response));

        return response;
    }

    @Transactional
    public TeamResponse update(Long id, TeamRequest request) {
        TeamEntity entity = getEntity(id);

        entity.setName(request.name());
        entity.setCountry(request.country());
        entity.setCoach(request.coach());

        TeamResponse response = toResponse(
                teamRepository.save(entity)
        );

        afterCommit(() -> eventPublisher.publishUpdated(response));

        return response;
    }

    @Transactional
    public TeamResponse patchTeam(Long id, PatchTeamRequest request) {
        TeamEntity entity = getEntity(id);

        if (request.name() != null) {
            entity.setName(request.name());
        }

        if (request.country() != null) {
            entity.setCountry(request.country());
        }

        if (request.coach() != null) {
            entity.setCoach(request.coach());
        }

        TeamResponse response = toResponse(
                teamRepository.save(entity)
        );

        afterCommit(() -> eventPublisher.publishUpdated(response));

        return response;
    }

    @Transactional
    public void delete(Long id) {
        TeamEntity entity = getEntity(id);
        TeamResponse response = toResponse(entity);

        // До V2 матчи ещё хранятся в памяти.
        List<Long> matchIds = storage.matches.values()
                .stream()
                .filter(match -> containsTeam(match, id))
                .map(MatchResponse::getId)
                .toList();

        teamRepository.delete(entity);

        afterCommit(() -> {
            matchIds.forEach(storage.matches::remove);
            eventPublisher.publishDeleted(response, matchIds.size());
        });
    }

    private TeamEntity getEntity(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Team", id)
                );
    }

    private TeamResponse toResponse(TeamEntity entity) {
        return TeamResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .country(entity.getCountry())
                .coach(entity.getCoach())
                .build();
    }

    private boolean containsTeam(MatchResponse match, Long teamId) {
        return (match.getHomeTeam() != null
                && teamId.equals(match.getHomeTeam().getId()))
                || (match.getAwayTeam() != null
                && teamId.equals(match.getAwayTeam().getId()));
    }

    private void afterCommit(Runnable action) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        action.run();
                    }
                }
        );
    }
}
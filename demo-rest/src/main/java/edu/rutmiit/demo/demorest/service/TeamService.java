package edu.rutmiit.demo.demorest.service;

import edu.rutmiit.demo.demorest.entity.TeamEntity;
import edu.rutmiit.demo.demorest.event.TeamEventPublisher;
import edu.rutmiit.demo.demorest.repository.TeamRepository;
import edu.rutmiit.demo.demorest.storage.InMemoryStorage;
import edu.rutmiit.demo.footballscoreapicontract.dto.PagedResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.PatchTeamRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamResponse;
import edu.rutmiit.demo.footballscoreapicontract.exception.ResourceNotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class TeamService {

    private final TeamRepository teamRepository;

    // Пока оставляем только потому, что матчи ещё in-memory.
    private final InMemoryStorage storage;

    private final MatchService matchService;
    private final TeamEventPublisher eventPublisher;

    public TeamService(
            TeamRepository teamRepository,
            InMemoryStorage storage,
            @Lazy MatchService matchService,
            TeamEventPublisher eventPublisher
    ) {
        this.teamRepository = teamRepository;
        this.storage = storage;
        this.matchService = matchService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public PagedResponse<TeamResponse> findAll(int page, int size) {

        List<TeamResponse> all = teamRepository.findAll().stream()
                .sorted(Comparator.comparingLong(TeamEntity::getId))
                .map(this::toResponse)
                .toList();

        int totalElements = all.size();
        int totalPages = size > 0
                ? (int) Math.ceil((double) totalElements / size)
                : 1;

        int from = page * size;
        int to = Math.min(from + size, totalElements);

        List<TeamResponse> content =
                (from >= totalElements)
                        ? List.of()
                        : all.subList(from, to);

        return new PagedResponse<>(
                content,
                page,
                size,
                totalElements,
                totalPages,
                page >= totalPages - 1
        );
    }

    @Transactional(readOnly = true)
    public TeamResponse findById(Long id) {
        return toResponse(findEntityById(id));
    }

    @Transactional
    public TeamResponse create(TeamRequest request) {

        if (teamRepository.existsByName(request.name())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Команда с таким названием уже существует"
            );
        }

        Long id = teamRepository.nextTeamId();

        TeamEntity entity = new TeamEntity(
                UUID.randomUUID(),
                id,
                request.name(),
                request.country(),
                request.coach()
        );

        try {
            entity = teamRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Команда с таким названием уже существует"
            );
        }

        TeamResponse response = toResponse(entity);

        /*
         * RabbitMQ вызываем только после успешного COMMIT БД.
         * Так транзакция PostgreSQL не держится открытой во время
         * сетевого вызова.
         */
        TeamResponse finalResponse = response;

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishCreated(finalResponse);
                    }
                }
        );

        return response;
    }

    @Transactional
    public TeamResponse update(Long id, TeamRequest request) {

        TeamEntity entity = findEntityById(id);

        if (!entity.getName().equals(request.name())
                && teamRepository.existsByName(request.name())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Команда с таким названием уже существует"
            );
        }

        entity.setName(request.name());
        entity.setCountry(request.country());
        entity.setCoach(request.coach());

        try {
            return toResponse(teamRepository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Команда с таким названием уже существует"
            );
        }
    }

    @Transactional
    public TeamResponse patchTeam(Long id, PatchTeamRequest request) {

        TeamEntity entity = findEntityById(id);

        if (request.name() != null
                && !request.name().equals(entity.getName())
                && teamRepository.existsByName(request.name())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Команда с таким названием уже существует"
            );
        }

        if (request.name() != null) {
            entity.setName(request.name());
        }

        if (request.country() != null) {
            entity.setCountry(request.country());
        }

        if (request.coach() != null) {
            entity.setCoach(request.coach());
        }

        try {
            return toResponse(teamRepository.saveAndFlush(entity));
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Команда с таким названием уже существует"
            );
        }
    }

    @Transactional
    public void delete(Long id) {

        TeamEntity entity = findEntityById(id);
        TeamResponse response = toResponse(entity);

        int matchesCount = (int) storage.matches.values().stream()
                .filter(m ->
                        (m.getHomeTeam() != null
                                && m.getHomeTeam().getId().equals(id))
                                ||
                                (m.getAwayTeam() != null
                                        && m.getAwayTeam().getId().equals(id))
                )
                .count();

        matchService.deleteMatchesByTeamId(id);

        teamRepository.delete(entity);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventPublisher.publishDeleted(
                                response,
                                matchesCount
                        );
                    }
                }
        );
    }

    private TeamEntity findEntityById(Long id) {
        return teamRepository.findByPublicId(id)
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
}
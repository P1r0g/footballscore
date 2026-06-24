package edu.rutmiit.demo.demorest.service;

import edu.rutmiit.demo.demorest.event.TeamEventPublisher;
import edu.rutmiit.demo.demorest.storage.InMemoryStorage;
import edu.rutmiit.demo.footballscoreapicontract.dto.PagedResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.PatchTeamRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamResponse;
import edu.rutmiit.demo.footballscoreapicontract.exception.ResourceNotFoundException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class TeamService {
    private final InMemoryStorage storage;
    private final MatchService matchService;
    private final TeamEventPublisher eventPublisher;

    public TeamService(InMemoryStorage storage,
                       @Lazy MatchService matchService,
                       TeamEventPublisher eventPublisher) {
        this.storage = storage;
        this.matchService = matchService;
        this.eventPublisher = eventPublisher;
    }

    public PagedResponse<TeamResponse> findAll(int page, int size) {
        List<TeamResponse> all = storage.teams.values().stream()
                .sorted(Comparator.comparingLong(TeamResponse::getId))
                .toList();
        int totalElements = all.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<TeamResponse> content = (from >= totalElements) ? List.of() : all.subList(from, to);
        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }

    public TeamResponse findById(Long id) {
        return Optional.ofNullable(storage.teams.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
    }

    public TeamResponse create(TeamRequest request) {
        long id = storage.teamSequence.incrementAndGet();
        TeamResponse team = TeamResponse.builder()
                .id(id)
                .name(request.name())
                .country(request.country())
                .coach(request.coach())
                .build();
        storage.teams.put(id, team);
        eventPublisher.publishCreated(team);
        return team;
    }

    public TeamResponse update(Long id, TeamRequest request) {
        if (storage.teams.get(id) != null) {
            TeamResponse updatedTeam = TeamResponse.builder()
                    .id(id)
                    .name(request.name())
                    .country(request.country())
                    .coach(request.coach())
                    .build();
            storage.teams.put(id, updatedTeam);
            return updatedTeam;
        } else throw new ResourceNotFoundException("Team", id);
    }

    public TeamResponse patchTeam(Long id, PatchTeamRequest request) {
        if (storage.teams.get(id) != null) {
            String newName = request.name() != null ? request.name() : storage.teams.get(id).getName();
            String newCountry = request.country() != null ? request.country() : storage.teams.get(id).getCountry();
            String newCoach = request.coach() != null ? request.coach() : storage.teams.get(id).getCoach();
            TeamResponse updatedTeam = TeamResponse.builder()
                    .id(id)
                    .name(newName)
                    .country(newCoach)
                    .coach(newCountry)
                    .build();
            storage.teams.put(id, updatedTeam);
            return updatedTeam;
        } else throw new ResourceNotFoundException("Team", id);
    }

    public void delete(Long id) {
        TeamResponse team = findById(id);

        int matchesCount = (int) storage.matches.values().stream()
                .filter(m -> m.getHomeTeam() != null && m.getHomeTeam().getId().equals(id) && m.getAwayTeam() != null && m.getAwayTeam().getId().equals(id))
                .count();

        matchService.deleteMatchesByTeamId(id);
        storage.teams.remove(id);
        eventPublisher.publishDeleted(team, matchesCount);
    }
}

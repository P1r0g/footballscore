package edu.rutmiit.demo.demorest.service;

import edu.rutmiit.demo.demorest.entity.MatchEntity;
import edu.rutmiit.demo.demorest.entity.TeamEntity;
import edu.rutmiit.demo.demorest.event.MatchEventPublisher;
import edu.rutmiit.demo.demorest.repository.MatchRepository;
import edu.rutmiit.demo.demorest.repository.TeamRepository;
import edu.rutmiit.demo.footballscoreapicontract.dto.MatchStatus;
import edu.rutmiit.demo.footballscoreapicontract.dto.PagedResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.GoalRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchStatusRequest;
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
public class MatchService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final MatchEventPublisher eventPublisher;

    public MatchService(
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            MatchEventPublisher eventPublisher
    ) {
        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.eventPublisher = eventPublisher;
    }

    public MatchResponse findMatchById(Long id) {
        MatchEntity entity = matchRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Match", id)
                );

        return toResponse(entity);
    }

    public PagedResponse<MatchResponse> findAllMatches(
            Long teamId,
            MatchStatus status,
            int page,
            int size
    ) {
        Page<MatchEntity> result = matchRepository.findWithFilters(
                teamId,
                status,
                PageRequest.of(page, size, Sort.by("id"))
        );

        List<MatchResponse> content = result.getContent()
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

    @Transactional
    public MatchResponse createMatch(MatchRequest request) {
        if (request.homeTeamId() == null || request.awayTeamId() == null) {
            throw new IllegalArgumentException(
                    "Нужно указать обе команды"
            );
        }

        if (request.homeTeamId().equals(request.awayTeamId())) {
            throw new IllegalArgumentException(
                    "Команда не может играть сама с собой"
            );
        }

        TeamEntity homeTeam = getTeam(request.homeTeamId());
        TeamEntity awayTeam = getTeam(request.awayTeamId());

        MatchEntity entity = new MatchEntity();
        entity.setHomeTeam(homeTeam);
        entity.setAwayTeam(awayTeam);
        entity.setHomeScore((short) 0);
        entity.setAwayScore((short) 0);
        entity.setStatus(MatchStatus.SCHEDULED);

        MatchResponse response = toResponse(
                matchRepository.save(entity)
        );

        afterCommit(() -> eventPublisher.publishCreated(response));

        return response;
    }

    @Transactional
    public void deleteMatch(Long id) {
        MatchEntity entity = getForUpdate(id);
        MatchResponse response = toResponse(entity);

        matchRepository.delete(entity);

        afterCommit(() -> eventPublisher.publishDeleted(
                response.getId(),
                response.getHomeTeam().getName(),
                response.getAwayTeam().getName()
        ));
    }

    @Transactional
    public void deleteMatchesByTeamId(Long teamId) {
        matchRepository.deleteByTeamId(teamId);
    }

    @Transactional
    public MatchResponse addGoal(Long matchId, GoalRequest request) {
        MatchEntity entity = getForUpdate(matchId);

        if (entity.getStatus() != MatchStatus.LIVE) {
            throw new IllegalStateException(
                    "Голы можно засчитывать только во время LIVE матча"
            );
        }

        boolean isHomeTeam =
                entity.getHomeTeam().getId().equals(request.teamId());

        boolean isAwayTeam =
                entity.getAwayTeam().getId().equals(request.teamId());

        if (!isHomeTeam && !isAwayTeam) {
            throw new IllegalStateException(
                    "Команда не участвует в данном матче"
            );
        }

        String teamName;

        if (isHomeTeam) {
            entity.setHomeScore(incrementScore(entity.getHomeScore()));
            teamName = entity.getHomeTeam().getName();
        } else {
            entity.setAwayScore(incrementScore(entity.getAwayScore()));
            teamName = entity.getAwayTeam().getName();
        }

        MatchResponse response = toResponse(
                matchRepository.save(entity)
        );

        afterCommit(() ->
                eventPublisher.publishGoalScored(response, teamName)
        );

        return response;
    }

    @Transactional
    public MatchResponse updateMatchStatus(
            Long matchId,
            MatchStatusRequest request
    ) {
        MatchEntity entity = getForUpdate(matchId);

        MatchStatus current = entity.getStatus();
        MatchStatus next = request.status();

        boolean validTransition =
                (current == MatchStatus.SCHEDULED
                        && (next == MatchStatus.LIVE
                        || next == MatchStatus.CANCELLED))
                        || (current == MatchStatus.LIVE
                        && (next == MatchStatus.HALFTIME
                        || next == MatchStatus.FINISHED))
                        || (current == MatchStatus.HALFTIME
                        && next == MatchStatus.LIVE);

        if (!validTransition) {
            throw new IllegalStateException(
                    "Недопустимый переход статуса: "
                            + current + " -> " + next
            );
        }

        entity.setStatus(next);

        MatchResponse response = toResponse(
                matchRepository.save(entity)
        );

        afterCommit(() ->
                eventPublisher.publishStatusChanged(matchId, current, next)
        );

        return response;
    }

    private MatchEntity getForUpdate(Long id) {
        return matchRepository.findByIdForUpdate(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Match", id)
                );
    }

    private TeamEntity getTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Team", id)
                );
    }

    private short incrementScore(short score) {
        if (score == Short.MAX_VALUE) {
            throw new IllegalStateException(
                    "Достигнуто максимальное значение счёта"
            );
        }

        return (short) (score + 1);
    }

    private MatchResponse toResponse(MatchEntity entity) {
        return MatchResponse.builder()
                .id(entity.getId())
                .homeTeam(toTeamResponse(entity.getHomeTeam()))
                .awayTeam(toTeamResponse(entity.getAwayTeam()))
                .homeScore(entity.getHomeScore())
                .awayScore(entity.getAwayScore())
                .status(entity.getStatus())
                .build();
    }

    private TeamResponse toTeamResponse(TeamEntity entity) {
        return TeamResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .country(entity.getCountry())
                .coach(entity.getCoach())
                .stadium(entity.getStadium())
                .build();
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
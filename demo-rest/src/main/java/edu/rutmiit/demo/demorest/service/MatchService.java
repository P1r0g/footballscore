package edu.rutmiit.demo.demorest.service;

import edu.rutmiit.demo.footballscoreapicontract.dto.*;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.GoalRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchStatusRequest;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamResponse;
import edu.rutmiit.demo.footballscoreapicontract.exception.ResourceNotFoundException;
import edu.rutmiit.demo.demorest.event.MatchEventPublisher;
import edu.rutmiit.demo.demorest.storage.InMemoryStorage;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class MatchService {

    private final InMemoryStorage storage;
    private final TeamService teamService;
    private final MatchEventPublisher eventPublisher;

    public MatchService(InMemoryStorage storage,
                        @Lazy TeamService teamService,
                        MatchEventPublisher eventPublisher) {
        this.storage = storage;
        this.teamService = teamService;
        this.eventPublisher = eventPublisher;
    }

    public MatchResponse findMatchById(Long id) {
        return Optional.ofNullable(storage.matches.get(id))
                .orElseThrow(() -> new ResourceNotFoundException("Match", id));
    }

    public PagedResponse<MatchResponse> findAllMatches(Long teamId, MatchStatus status, int page, int size) {
        Stream<MatchResponse> stream = storage.matches.values().stream()
                .sorted((m1, m2) -> m1.getId().compareTo(m2.getId()));

        if (teamId != null) {
            stream = stream.filter(m -> m.getHomeTeam() != null && m.getHomeTeam().getId().equals(teamId) && m.getAwayTeam() != null && m.getAwayTeam().getId().equals(teamId));
        }
        if (status != null) {
            stream = stream.filter(m -> m.getStatus().equals(status));
        }

        List<MatchResponse> allMatches = stream.toList();
        int totalElements = allMatches.size();
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 1;
        int from = page * size;
        int to = Math.min(from + size, totalElements);
        List<MatchResponse> content = (from >= totalElements) ? List.of() : allMatches.subList(from, to);
        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }

    public MatchResponse createMatch(MatchRequest request) {

        TeamResponse homeTeam = teamService.findById(request.homeTeamId());
        TeamResponse awayTeam = teamService.findById(request.awayTeamId());

        long id = storage.matchSequence.incrementAndGet();
        MatchResponse match = MatchResponse.builder()
                .id(id)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .homeScore((short) 0)
                .awayScore((short) 0)
                .status(MatchStatus.SCHEDULED)
                .build();
        storage.matches.put(id, match);

        // Публикуем доменное событие ПОСЛЕ успешного сохранения.
        // Если RabbitMQ недоступен — книга всё равно создана, событие просто потеряется.
        eventPublisher.publishCreated(match);

        return match;
    }

//    public MatchResponse updateBook(Long id, UpdateBookRequest request) {
//        MatchResponse existing = findBookById(id);
//        validateIsbn(request.isbn(), id);
//
//        MatchResponse updated = MatchResponse.builder()
//                .id(id)
//                .title(request.title())
//                .isbn(request.isbn())
//                .author(existing.getAuthor())
//                .description(request.description())
//                .genre(request.genre())
//                .publishedYear(request.publishedYear())
//                .language(request.language())
//                .createdAt(existing.getCreatedAt())
//                .updatedAt(LocalDateTime.now())
//                .build();
//        storage.books.put(id, updated);
//        eventPublisher.publishUpdated(updated);
//        return updated;
//    }
//
//    public MatchResponse patchBook(Long id, PatchMatchRequest request) {
//        MatchResponse existing = findBookById(id);
//
//        // ISBN меняется — проверяем уникальность, исключая текущую книгу
//        if (request.isbn() != null && !request.isbn().equalsIgnoreCase(existing.getIsbn())) {
//            validateIsbn(request.isbn(), id);
//        }
//
//        MatchResponse updated = MatchResponse.builder()
//                .id(id)
//                .title(request.title() != null ? request.title() : existing.getTitle())
//                .isbn(request.isbn() != null ? request.isbn() : existing.getIsbn())
//                .author(existing.getAuthor())
//                .description(request.description() != null ? request.description() : existing.getDescription())
//                .genre(request.genre() != null ? request.genre() : existing.getGenre())
//                .publishedYear(request.publishedYear() != null ? request.publishedYear() : existing.getPublishedYear())
//                .language(request.language() != null ? request.language() : existing.getLanguage())
//                .createdAt(existing.getCreatedAt())
//                .updatedAt(LocalDateTime.now())
//                .build();
//        storage.books.put(id, updated);
//        eventPublisher.publishUpdated(updated);
//        return updated;
//    }

    public void deleteMatch(Long id) {
        MatchResponse match = findMatchById(id);
        storage.matches.remove(id);
        eventPublisher.publishDeleted(id, match.getHomeTeam().getName(), match.getAwayTeam().getName());
    }

    public void deleteMatchesByTeamId(Long teamId) {
        List<Long> toDelete = storage.matches.values().stream()
                .filter(m -> m.getHomeTeam() != null && m.getAwayTeam() != null && m.getHomeTeam().getId().equals(teamId) && m.getAwayTeam().getId().equals(teamId))
                .map(MatchResponse::getId)
                .toList();
        toDelete.forEach(storage.matches::remove);
    }

    public MatchResponse addGoal(Long matchId, GoalRequest request) {

        MatchResponse match = findMatchById(matchId);

        if (match.getStatus() != MatchStatus.LIVE) {
            throw new IllegalStateException("Голы можно засчитывать только во время LIVE матча");
        }

        boolean isHomeTeam = match.getHomeTeam().getId().equals(request.teamId());
        boolean isAwayTeam = match.getAwayTeam().getId().equals(request.teamId());

        if (!isHomeTeam && !isAwayTeam) {
            throw new IllegalStateException("Команда не участвует в данном матче");
        }

        MatchResponse updated = MatchResponse.builder()
                .id(match.getId())
                .homeTeam(match.getHomeTeam())
                .awayTeam(match.getAwayTeam())
                .homeScore(isHomeTeam
                        ? (short) (match.getHomeScore() + 1)
                        : match.getHomeScore())
                .awayScore(isAwayTeam
                        ? (short) (match.getAwayScore() + 1)
                        : match.getAwayScore())
                .status(match.getStatus())
                .build();

        storage.matches.put(matchId, updated);

        eventPublisher.publishGoalScored(updated, storage.teams.get(request.teamId()).getName());

        return updated;
    }

    public MatchResponse updateMatchStatus(Long matchId, MatchStatusRequest request) {

        MatchResponse match = findMatchById(matchId);

        MatchStatus current = match.getStatus();
        MatchStatus next = request.status();

        boolean validTransition =
                (current == MatchStatus.SCHEDULED &&
                        (next == MatchStatus.LIVE || next == MatchStatus.CANCELLED))
                        ||
                        (current == MatchStatus.LIVE &&
                                (next == MatchStatus.HALFTIME || next == MatchStatus.FINISHED))
                        ||
                        (current == MatchStatus.HALFTIME && next == MatchStatus.LIVE);

        if (!validTransition) {
            throw new IllegalStateException(
                    "Недопустимый переход статуса: " + current + " -> " + next
            );
        }

        MatchResponse updated = MatchResponse.builder()
                .id(match.getId())
                .homeTeam(match.getHomeTeam())
                .awayTeam(match.getAwayTeam())
                .homeScore(match.getHomeScore())
                .awayScore(match.getAwayScore())
                .status(next)
                .build();

        storage.matches.put(matchId, updated);
        eventPublisher.publishStatusChanged(matchId, current, next);
        return updated;
    }

}

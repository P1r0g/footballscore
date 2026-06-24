package edu.rutmiit.demo.events;

import edu.rutmiit.demo.footballscoreapicontract.dto.MatchStatus;

/**
 * Семейство событий, связанных с матчами.
 */
public sealed interface MatchEvent {

    /**
     * Матч создан.
     */
    record Created(
            Long matchId,
            Long homeTeamId,
            String homeTeamName,
            Long awayTeamId,
            String awayTeamName,
            MatchStatus status
    ) implements MatchEvent {}

    /**
     * Статус матча изменился.
     */
    record StatusChanged(
            Long matchId,
            MatchStatus oldStatus,
            MatchStatus newStatus
    ) implements MatchEvent {}

    /**
     * Забит гол.
     */
    record GoalScored(
            Long matchId,
            String teamName,
            Short homeScore,
            Short awayScore
    ) implements MatchEvent {}

    /**
     * Матч удалён.
     */
    record Deleted(
            Long matchId,
            String homeTeamName,
            String awayTeamName
    ) implements MatchEvent {}

    record Enriched(
            Long matchId,
            String leader,
            Integer goalDifference
    ) implements MatchEvent {}
}
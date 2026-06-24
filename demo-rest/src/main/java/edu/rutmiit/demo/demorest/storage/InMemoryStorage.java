package edu.rutmiit.demo.demorest.storage;

import edu.rutmiit.demo.footballscoreapicontract.dto.MatchStatus;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamResponse;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static graphql.introspection.IntrospectionQueryBuilder.build;

@Component
public class InMemoryStorage {
    public final Map<Long, TeamResponse> teams = new ConcurrentHashMap<>();
    public final Map<Long, MatchResponse> matches = new ConcurrentHashMap<>();

    public final AtomicLong teamSequence = new AtomicLong(0);
    public final AtomicLong matchSequence = new AtomicLong(0);

    @PostConstruct
    public void init() {
        // Создаем команды с полным набором полей (демонстрация Lombok @Builder)
        TeamResponse team1 = TeamResponse.builder()
                .id(teamSequence.incrementAndGet())
                .name("Реал Мадрид")
                .country("Испания")
                .coach("Жозе Моуриньо")
                .build();

        TeamResponse team2 = TeamResponse.builder()
                .id(teamSequence.incrementAndGet())
                .name("Барселона")
                .country("Испания")
                .coach("Ханси Флик")
                .build();

        TeamResponse team3 = TeamResponse.builder()
                .id(teamSequence.incrementAndGet())
                .name("Ливерпуль")
                .country("Англия")
                .coach("Юрген Клопп")
                .build();

        TeamResponse team4 = TeamResponse.builder()
                .id(teamSequence.incrementAndGet())
                .name("Бавария")
                .country("Германия")
                .coach("Винсант Компани")
                .build();

        teams.put(team1.getId(), team1);
        teams.put(team2.getId(), team2);
        teams.put(team3.getId(), team3);
        teams.put(team4.getId(), team4);

        // Создаем матчей с полным набором полей
        long match1 = matchSequence.incrementAndGet();
        matches.put(match1, MatchResponse.builder()
                .id(match1)
                .homeTeam(team1)
                .awayTeam(team2)
                .homeScore((short) 0)
                .awayScore((short) 0)
                .status(MatchStatus.SCHEDULED)
                .build());

        long match2 = matchSequence.incrementAndGet();
        matches.put(match2, MatchResponse.builder()
                .id(match2)
                .homeTeam(team3)
                .awayTeam(team4)
                .homeScore((short) 1)
                .awayScore((short) 0)
                .status(MatchStatus.LIVE)
                .build());
    }
}
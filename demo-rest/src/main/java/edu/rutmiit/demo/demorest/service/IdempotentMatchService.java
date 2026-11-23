package edu.rutmiit.demo.demorest.service;

import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class IdempotentMatchService {

    private static final String OPERATION = "create-match-v1";

    private final MatchService matchService;
    private final JdbcTemplate jdbc;
    private final JsonMapper jsonMapper;

    public IdempotentMatchService(MatchService matchService, JdbcTemplate jdbc, JsonMapper jsonMapper) {
        this.matchService = matchService;
        this.jdbc = jdbc;
        this.jsonMapper = jsonMapper;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Result create(String principal, UUID key, MatchRequest request) {
        String hash = fingerprint(request);
        jdbc.execute("SET LOCAL lock_timeout = '1000ms'");

        int claimed;
        try {
            claimed = jdbc.update("""
                    INSERT INTO match_idempotency
                        (principal_id, operation, operation_key, request_fingerprint)
                    VALUES (?, ?, ?, ?)
                    ON CONFLICT (principal_id, operation, operation_key) DO NOTHING
                    """, principal, OPERATION, key, hash);
        } catch (DataAccessException failure) {
            if (failure.getMostSpecificCause() instanceof SQLException sql
                    && "55P03".equals(sql.getSQLState())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Операция с этим ключом ещё выполняется", failure);
            }
            throw failure;
        }

        if (claimed == 0) {
            return replay(principal, key, hash);
        }

        var match = matchService.createMatch(request);
        String snapshot = jsonMapper.writeValueAsString(match);
        String body = jdbc.queryForObject("""
                UPDATE match_idempotency
                SET state = 'COMPLETED', match_id = ?, response_body = CAST(? AS jsonb)
                WHERE principal_id = ? AND operation = ? AND operation_key = ?
                RETURNING response_body::text
                """, String.class, match.getId(), snapshot, principal, OPERATION, key);

        return new Result(match.getId(), body, false);
    }

    private Result replay(String principal, UUID key, String hash) {
        Stored stored = jdbc.queryForObject("""
                SELECT request_fingerprint, state, match_id, response_body::text,
                       expires_at <= clock_timestamp() AS expired
                FROM match_idempotency
                WHERE principal_id = ? AND operation = ? AND operation_key = ?
                """, (row, index) -> new Stored(
                row.getString("request_fingerprint"),
                row.getString("state"),
                row.getObject("match_id", Long.class),
                row.getString("response_body"),
                row.getBoolean("expired")), principal, OPERATION, key);

        if (stored.expired()) {
            throw new ResponseStatusException(HttpStatus.GONE, "Срок ключа истёк");
        }
        if (!stored.hash().equals(hash)) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_CONTENT,
                    "Ключ уже использован с другим телом запроса");
        }
        if (!"COMPLETED".equals(stored.state())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Операция с этим ключом ещё выполняется");
        }
        return new Result(stored.matchId(), stored.body(), true);
    }

    private static String fingerprint(MatchRequest request) {
        String normalized = "homeTeamId=" + request.homeTeamId() + "\nawayTeamId=" + request.awayTeamId();
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException failure) {
            throw new IllegalStateException("SHA-256 недоступен", failure);
        }
    }

    private record Stored(String hash, String state, Long matchId, String body, boolean expired) {}

    public record Result(long matchId, String body, boolean replayed) {}
}
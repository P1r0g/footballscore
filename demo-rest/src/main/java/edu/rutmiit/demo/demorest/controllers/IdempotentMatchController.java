package edu.rutmiit.demo.demorest.controllers;

import edu.rutmiit.demo.demorest.service.IdempotentMatchService;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchRequest;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.UUID;

@RestController
@Profile("!session-auth")
public class IdempotentMatchController {

    private final IdempotentMatchService matches;

    public IdempotentMatchController(IdempotentMatchService matches) {
        this.matches = matches;
    }

    @PostMapping(path = "/api/matches/idempotent",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('EDITOR')")
    public ResponseEntity<String> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "Idempotency-Key", required = false) String rawKey,
            @Valid @RequestBody MatchRequest request) {
        if (jwt == null || jwt.getSubject() == null || jwt.getSubject().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "В JWT отсутствует sub");
        }
        if (rawKey == null || !rawKey.matches("(?i)^[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key должен быть UUID");
        }

        var result = matches.create(jwt.getSubject(), UUID.fromString(rawKey), request);
        return ResponseEntity.created(URI.create("/api/matches/" + result.matchId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Replayed", Boolean.toString(result.replayed()))
                .body(result.body());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ProblemDetail> handleStatus(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(exception.getBody());
    }
}
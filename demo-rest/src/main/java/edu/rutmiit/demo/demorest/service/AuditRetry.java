package edu.rutmiit.demo.demorest.service;

import edu.rutmiit.demo.demorest.exception.AuditUpstreamException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.NoHttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import javax.net.ssl.SSLException;
import java.io.EOFException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

@Component
public class AuditRetry {

    private static final Logger log = LoggerFactory.getLogger(AuditRetry.class);
    private static final int MAX_ATTEMPTS = 2;

    private final long attemptAllowanceMs;
    private final long budgetMs;
    private final long reserveMs;

    public AuditRetry(
            @Value("${audit.service.pool-acquire-timeout-ms}") long poolTimeoutMs,
            @Value("${audit.service.connect-timeout-ms}") long connectTimeoutMs,
            @Value("${audit.service.read-timeout-ms}") long readTimeoutMs,
            @Value("${audit.service.retry-budget-ms}") long budgetMs,
            @Value("${audit.service.retry-reserve-ms}") long reserveMs) {
        this.attemptAllowanceMs = poolTimeoutMs + connectTimeoutMs + readTimeoutMs;
        this.budgetMs = budgetMs;
        this.reserveMs = reserveMs;
    }

    public String execute(Supplier<String> request) {
        long started = System.nanoTime();
        var attempts = new AtomicInteger();
        var delayMs = new AtomicLong();
        var refusedByBudget = new AtomicBoolean();

        var config = RetryConfig.<String>custom()
                .maxAttempts(MAX_ATTEMPTS)
                .retryOnException(failure -> {
                    if (attempts.get() >= MAX_ATTEMPTS || !classify(failure).retryable) {
                        return false;
                    }
                    long delay = retryDelayMs(failure);
                    boolean fits = fitsBudget(started, delay);
                    refusedByBudget.set(!fits);
                    delayMs.set(delay);
                    return fits;
                })
                .intervalBiFunction((attempt, outcome) -> delayMs.get())
                .build();

        var retry = Retry.of("audit", config);
        retry.getEventPublisher().onRetry(event ->
                log.atWarn().addKeyValue("event", "audit.retry")
                        .addKeyValue("attempt", attempts.get() + 1)
                        .addKeyValue("delayMs", delayMs.get())
                        .log("Повтор запроса журнала аудита"));

        try {
            return retry.executeSupplier(() -> {
                if (!fitsBudget(started, 0)) {
                    throw budgetExceeded(null);
                }
                log.atInfo().addKeyValue("event", "audit.attempt")
                        .addKeyValue("attempt", attempts.incrementAndGet())
                        .log("Чтение журнала аудита");
                return request.get();
            });
        } catch (RestClientException failure) {
            if (refusedByBudget.get()) {
                throw budgetExceeded(failure);
            }
            var outcome = classify(failure);
            throw new AuditUpstreamException(outcome.status, "Сервис аудита недоступен", failure);
        }
    }

    private boolean fitsBudget(long started, long waitMs) {
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        return waitMs <= budgetMs - elapsedMs - attemptAllowanceMs - reserveMs;
    }

    private static AuditUpstreamException budgetExceeded(Throwable cause) {
        return new AuditUpstreamException(
                HttpStatus.GATEWAY_TIMEOUT, "Исчерпан бюджет вызова сервиса аудита", cause);
    }

    private static Failure classify(Throwable failure) {
        if (Thread.currentThread().isInterrupted()) {
            return Failure.OTHER;
        }
        for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
            if (cause instanceof RestClientResponseException response) {
                return switch (response.getStatusCode().value()) {
                    case 504 -> Failure.TIMEOUT;
                    case 429, 502, 503 -> Failure.TEMPORARY;
                    default -> Failure.OTHER;
                };
            }
            if (cause instanceof SSLException || cause instanceof UnknownHostException
                    || cause instanceof InterruptedException) {
                return Failure.OTHER;
            }
            if (cause instanceof SocketTimeoutException) {
                return Failure.TIMEOUT;
            }
            if (cause instanceof SocketException || cause instanceof EOFException
                    || cause instanceof NoHttpResponseException || cause instanceof ConnectionClosedException) {
                return Failure.TEMPORARY;
            }
        }
        return Failure.OTHER;
    }

    private static long retryDelayMs(Throwable failure) {
        long jitteredDelay = ThreadLocalRandom.current().nextLong(75, 226);
        if (failure instanceof RestClientResponseException response && response.getResponseHeaders() != null) {
            return Math.max(jitteredDelay, serverWaitMs(response.getResponseHeaders().getFirst("Retry-After")));
        }
        return jitteredDelay;
    }

    private static long serverWaitMs(String header) {
        if (header == null || header.isBlank()) {
            return 0;
        }
        String value = header.strip();
        if (value.matches("[0-9]+")) {
            try {
                return Math.multiplyExact(Long.parseLong(value), 1000L);
            } catch (ArithmeticException | NumberFormatException tooLarge) {
                return Long.MAX_VALUE;
            }
        }
        try {
            var retryAt = ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
            return Math.max(0, Duration.between(Instant.now(), retryAt).toMillis());
        } catch (DateTimeParseException malformed) {
            return 0;
        }
    }

    private enum Failure {
        TIMEOUT(true, HttpStatus.GATEWAY_TIMEOUT),
        TEMPORARY(true, HttpStatus.BAD_GATEWAY),
        OTHER(false, HttpStatus.BAD_GATEWAY);

        private final boolean retryable;
        private final HttpStatus status;

        Failure(boolean retryable, HttpStatus status) {
            this.retryable = retryable;
            this.status = status;
        }
    }
}
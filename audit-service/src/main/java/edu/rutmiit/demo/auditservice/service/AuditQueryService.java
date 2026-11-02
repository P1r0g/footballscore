package edu.rutmiit.demo.auditservice.service;

import edu.rutmiit.demo.auditservice.model.AuditEntry;
import edu.rutmiit.demo.auditservice.storage.AuditStorage;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AuditQueryService {

    private static final Logger log =
            LoggerFactory.getLogger(AuditQueryService.class);

    private final AuditStorage auditStorage;
    private final AuditDiagnosticMode diagnosticMode;
    private final Counter successfulQueries;
    private final Counter failedQueries;

    public AuditQueryService(
            AuditStorage auditStorage,
            AuditDiagnosticMode diagnosticMode,
            MeterRegistry registry) {

        this.auditStorage = auditStorage;
        this.diagnosticMode = diagnosticMode;

        successfulQueries = registry.counter(
                "footballscore.audit.queries",
                "result", "success"
        );

        failedQueries = registry.counter(
                "footballscore.audit.queries",
                "result", "failure"
        );
    }

    public Map<String, Object> getAuditLog(int limit) {
        try {
            diagnosticMode.apply();

            List<AuditEntry> entries = auditStorage.findLatest(limit);

            Map<String, Object> response = Map.of(
                    "totalEntries", auditStorage.count(),
                    "showing", entries.size(),
                    "entries", entries
            );

            successfulQueries.increment();

            log.atInfo()
                    .addKeyValue("event", "audit.query.completed")
                    .addKeyValue("outcome", "success")
                    .log("Audit log query completed");

            return response;
        } catch (RuntimeException exception) {
            failedQueries.increment();

            log.atError()
                    .addKeyValue("event", "audit.query.failed")
                    .addKeyValue("outcome", "failure")
                    .setCause(exception)
                    .log("Audit log query failed");

            throw exception;
        }
    }
}
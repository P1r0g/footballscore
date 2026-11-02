package edu.rutmiit.demo.auditservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Properties;

@Component
public class AuditDiagnosticMode {

    private static final Logger log =
            LoggerFactory.getLogger(AuditDiagnosticMode.class);

    private final boolean enabled;
    private final Path controlFile;

    public AuditDiagnosticMode(
            @Value("${footballscore.diagnostics.enabled:false}")
            boolean enabled,
            @Value("${footballscore.diagnostics.file:../runtime-controls/audit-fault.properties}")
            String controlFile) {

        this.enabled = enabled;
        this.controlFile = Path.of(controlFile);

        if (enabled) {
            log.warn("Audit diagnostics enabled; control file: {}",
                    this.controlFile.toAbsolutePath());
        }
    }

    public void apply() {
        Mode mode = currentMode();

        if (mode == Mode.FAIL) {
            log.atWarn()
                    .addKeyValue("event", "audit.diagnostic.fail")
                    .log("Simulated audit service failure");

            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Simulated audit service failure"
            );
        }

        if (mode == Mode.SLOW) {
            log.atWarn()
                    .addKeyValue("event", "audit.diagnostic.slow")
                    .addKeyValue("delayMs", 750)
                    .log("Simulated audit service delay");

            try {
                Thread.sleep(750);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();

                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Audit request interrupted",
                        exception
                );
            }
        }
    }

    private Mode currentMode() {
        if (!enabled || !Files.isRegularFile(controlFile)) {
            return Mode.NORMAL;
        }

        Properties properties = new Properties();

        try (Reader reader = Files.newBufferedReader(
                controlFile, StandardCharsets.UTF_8)) {

            properties.load(reader);

            Mode mode = Mode.valueOf(
                    properties.getProperty("mode", "NORMAL")
            );

            if (mode == Mode.NORMAL) {
                return Mode.NORMAL;
            }

            Instant now = Instant.now();
            Instant expiresAt = Instant.parse(
                    properties.getProperty("expiresAt")
            );

            if (!expiresAt.isAfter(now)
                    || expiresAt.isAfter(now.plusSeconds(120))) {
                return Mode.NORMAL;
            }

            return mode;
        } catch (IOException | RuntimeException exception) {
            log.warn("Cannot read diagnostic mode; using NORMAL",
                    exception);

            return Mode.NORMAL;
        }
    }

    private enum Mode {
        NORMAL,
        SLOW,
        FAIL
    }
}
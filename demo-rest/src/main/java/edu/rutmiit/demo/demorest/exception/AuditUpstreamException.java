package edu.rutmiit.demo.demorest.exception;

import org.springframework.http.HttpStatus;

public class AuditUpstreamException extends RuntimeException {

    private final HttpStatus status;

    public AuditUpstreamException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
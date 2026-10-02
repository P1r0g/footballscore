package edu.rutmiit.demo.demorest.controllers;

import edu.rutmiit.demo.demorest.service.AuditClientService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditProxyController {

    private final AuditClientService auditClientService;

    public AuditProxyController(AuditClientService auditClientService) {
        this.auditClientService = auditClientService;
    }

    @PreAuthorize("hasRole('EDITOR')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAuditLog() {
        return auditClientService.getAuditLog();
    }
}
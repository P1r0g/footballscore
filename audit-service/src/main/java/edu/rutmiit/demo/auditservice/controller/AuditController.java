package edu.rutmiit.demo.auditservice.controller;

import edu.rutmiit.demo.auditservice.service.AuditQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditQueryService auditQueryService;

    public AuditController(AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    @GetMapping
    public Map<String, Object> getAuditLog(
            @RequestParam(defaultValue = "100") int limit
    ) {
        return auditQueryService.getAuditLog(limit);
    }
}
package edu.rutmiit.demo.demorest.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SessionController {

    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken csrfToken) {
        return new CsrfResponse(
                csrfToken.getHeaderName(),
                csrfToken.getParameterName(),
                csrfToken.getToken()
        );
    }

    @GetMapping("/api/session/me")
    public SessionResponse me(Authentication authentication) {
        List<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();

        return new SessionResponse(
                authentication.getName(),
                authorities
        );
    }

    public record CsrfResponse(
            String headerName,
            String parameterName,
            String token
    ) {}

    public record SessionResponse(
            String username,
            List<String> authorities
    ) {}
}
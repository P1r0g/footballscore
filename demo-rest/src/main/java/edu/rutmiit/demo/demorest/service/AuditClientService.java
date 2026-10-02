package edu.rutmiit.demo.demorest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AuditClientService {

    private static final Logger log =
            LoggerFactory.getLogger(AuditClientService.class);

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final RestClient restClient;

    public AuditClientService(
            OAuth2AuthorizedClientManager authorizedClientManager,
            @Value("${audit.service.base-url}") String baseUrl) {

        this.authorizedClientManager = authorizedClientManager;

        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(10000);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public String getAuditLog() {
        var authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId("audit-client")
                .principal("demo-rest")
                .build();

        var authorizedClient =
                authorizedClientManager.authorize(authorizeRequest);

        if (authorizedClient == null) {
            throw new IllegalStateException(
                    "Не удалось получить сервисный токен для audit-service"
            );
        }

        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        log.info("Запрашиваем GET /api/audit у audit-service от имени demo-rest-client");

        String response = restClient.get()
                .uri("/api/audit")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .body(String.class);

        log.info("Ответ audit-service успешно получен");

        return response;
    }
}
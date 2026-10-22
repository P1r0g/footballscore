package edu.rutmiit.demo.demorest.service;

import edu.rutmiit.demo.demorest.config.RequestIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Service
public class AuditClientService {

    private static final Logger log =
            LoggerFactory.getLogger(AuditClientService.class);

    private final OAuth2AuthorizedClientManager authorizedClientManager;
    private final RestClient restClient;

    public AuditClientService(
            OAuth2AuthorizedClientManager authorizedClientManager,
            RestClient.Builder builder,
            @Value("${audit.service.base-url}") String baseUrl
    ) {
        this.authorizedClientManager = authorizedClientManager;

        var httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(500))
                .build();

        var requestFactory =
                new JdkClientHttpRequestFactory(httpClient);

        requestFactory.setReadTimeout(Duration.ofSeconds(2));

        this.restClient = builder
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .requestInterceptor((request, body, execution) -> {
                    String requestId = MDC.get(RequestIds.MDC_KEY);

                    if (requestId != null) {
                        request.getHeaders()
                                .set(RequestIds.HEADER, requestId);
                    }

                    return execution.execute(request, body);
                })
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

        String accessToken =
                authorizedClient.getAccessToken().getTokenValue();

        log.info("Calling audit-service GET /api/audit");

        String response = restClient.get()
                .uri("/api/audit")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .body(String.class);

        log.info("Ответ audit-service успешно получен");

        return response;
    }
}
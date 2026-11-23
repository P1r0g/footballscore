package edu.rutmiit.demo.demorest.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration(proxyBeanMethods = false)
public class AuditHttpClientConfig {

    @Bean
    HttpComponentsClientHttpRequestFactory auditRequestFactory(
            @Value("${audit.service.pool-acquire-timeout-ms}") long poolTimeoutMs,
            @Value("${audit.service.connect-timeout-ms}") long connectTimeoutMs,
            @Value("${audit.service.read-timeout-ms}") long readTimeoutMs) {
        return requestFactory(poolTimeoutMs, connectTimeoutMs, readTimeoutMs);
    }

    @Bean
    HttpComponentsClientHttpRequestFactory auditTokenRequestFactory(
            @Value("${audit.service.pool-acquire-timeout-ms}") long poolTimeoutMs,
            @Value("${audit.service.token-connect-timeout-ms}") long connectTimeoutMs,
            @Value("${audit.service.token-read-timeout-ms}") long readTimeoutMs) {
        return requestFactory(poolTimeoutMs, connectTimeoutMs, readTimeoutMs);
    }

    private HttpComponentsClientHttpRequestFactory requestFactory(
            long poolTimeoutMs, long connectTimeoutMs, long readTimeoutMs) {
        if (poolTimeoutMs <= 0 || connectTimeoutMs <= 0 || readTimeoutMs <= 0) {
            throw new IllegalArgumentException("Audit HTTP timeouts must be positive");
        }

        var readTimeout = Timeout.ofMilliseconds(readTimeoutMs);
        var connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(connectTimeoutMs))
                .setSocketTimeout(readTimeout)
                .build();
        var requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(poolTimeoutMs))
                .setResponseTimeout(readTimeout)
                .build();
        var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        var httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .disableAutomaticRetries()
                .disableRedirectHandling()
                .build();
        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }
}
package edu.rutmiit.demo.grpcenrichment.publisher;

import edu.rutmiit.demo.events.MatchEvent;
import edu.rutmiit.demo.events.EventEnvelope;
import edu.rutmiit.demo.events.RoutingKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Публикация событий обогащения (book.enriched) в RabbitMQ.
 *
 * Аналогичен BookEventPublisher в demo-rest, но публикует другой тип события.
 * Паттерн fire-and-forget: если RabbitMQ недоступен, ошибка логируется,
 * но gRPC-вызов уже выполнен — результат не теряется полностью.
 */
@Component
public class EnrichmentEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(EnrichmentEventPublisher.class);

    private static final String SOURCE =
            "grpc-enrichment-client";

    private final RabbitTemplate rabbitTemplate;

    public EnrichmentEventPublisher(
            RabbitTemplate rabbitTemplate
    ) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishEnriched(
            MatchEvent.Enriched enrichedEvent
    ) {

        try {

            EventEnvelope<MatchEvent> envelope =
                    EventEnvelope.wrap(
                            enrichedEvent,
                            SOURCE,
                            RoutingKeys.MATCH_ENRICHED
                    );

            rabbitTemplate.convertAndSend(
                    RoutingKeys.EXCHANGE,
                    RoutingKeys.MATCH_ENRICHED,
                    envelope
            );

            log.info(
                    "Событие отправлено: {} [matchId={}]",
                    RoutingKeys.MATCH_ENRICHED,
                    enrichedEvent.matchId()
            );

        } catch (Exception e) {

            log.error(
                    "Не удалось отправить событие {}: {}",
                    RoutingKeys.MATCH_ENRICHED,
                    e.getMessage()
            );
        }
    }
}
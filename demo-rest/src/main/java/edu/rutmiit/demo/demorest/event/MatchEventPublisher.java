package edu.rutmiit.demo.demorest.event;

import edu.rutmiit.demo.footballscoreapicontract.dto.MatchStatus;
import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchResponse;
import edu.rutmiit.demo.events.MatchEvent;
import edu.rutmiit.demo.events.EventEnvelope;
import edu.rutmiit.demo.events.RoutingKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Публикация доменных событий книг в RabbitMQ.
 *
 * Паттерн: BookService вызывает publish-метод ПОСЛЕ успешного завершения
 * бизнес-операции. Если RabbitMQ недоступен — событие логируется как ошибка,
 * но основная операция (создание/удаление книги) НЕ откатывается.
 *
 * Это паттерн «fire-and-forget» — допустимая потеря события лучше,
 * чем отказ бизнес-операции из-за недоступности брокера.
 *
 * В промышленных системах для гарантированной доставки используют:
 * - Transactional Outbox (запись события в БД в одной транзакции с данными),
 * - Change Data Capture (Debezium/Kafka Connect).
 */
@Component
public class MatchEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(MatchEventPublisher.class);

    private static final String SOURCE = "demo-rest";

    private final RabbitTemplate rabbitTemplate;

    public MatchEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishCreated(MatchResponse match) {
        var event = new MatchEvent.Created(
                match.getId(),
                match.getHomeTeam().getId(),
                match.getHomeTeam().getName(),
                match.getAwayTeam().getId(),
                match.getAwayTeam().getName(),
                match.getStatus()
        );
        send(RoutingKeys.MATCH_CREATED, event);
    }

    public void publishStatusChanged(Long matchId, MatchStatus oldStatus, MatchStatus newStatus) {
        var event = new MatchEvent.StatusChanged(
                matchId,
                oldStatus,
                newStatus
        );

        send(RoutingKeys.MATCH_STATUS_CHANGED, event);
    }

    public void publishGoalScored(MatchResponse match, String teamName) {
        var event = new MatchEvent.GoalScored(
                match.getId(),
                teamName,
                match.getHomeScore(),
                match.getAwayScore()
        );
        send(RoutingKeys.MATCH_GOAL_SCORED, event);
    }

    public void publishDeleted(Long matchId, String homeTeamName, String awayTeamName) {
        var event = new MatchEvent.Deleted(
                matchId,
                homeTeamName,
                awayTeamName

        );
        send(RoutingKeys.MATCH_DELETED, event);
    }

    private void send(String routingKey, MatchEvent event) {

        try {

            EventEnvelope<MatchEvent> envelope =
                    EventEnvelope.wrap(
                            event,
                            SOURCE,
                            routingKey
                    );

            rabbitTemplate.convertAndSend(
                    RoutingKeys.EXCHANGE,
                    routingKey,
                    envelope
            );

            log.info(
                    "Событие отправлено: {} [eventId={}]",
                    routingKey,
                    envelope.metadata().eventId()
            );

        } catch (Exception e) {

            log.error(
                    "Не удалось отправить событие {}: {}",
                    routingKey,
                    e.getMessage()
            );
        }
    }
}

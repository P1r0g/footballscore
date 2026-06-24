package edu.rutmiit.demo.demorest.event;

import edu.rutmiit.demo.events.TeamEvent;
import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamResponse;
import edu.rutmiit.demo.events.EventEnvelope;
import edu.rutmiit.demo.events.RoutingKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Публикация доменных событий авторов в RabbitMQ.
 *
 * Аналогичен BookEventPublisher — тот же fire-and-forget паттерн.
 */
@Component
public class TeamEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TeamEventPublisher.class);
    private static final String SOURCE = "demo-rest";

    private final RabbitTemplate rabbitTemplate;

    public TeamEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Публикует событие «команда создана».
     */
    public void publishCreated(TeamResponse team) {
        var event = new TeamEvent.Created(
                team.getId(),
                team.getName(),
                team.getCountry(),
                team.getCoach()

        );
        send(RoutingKeys.TEAM_CREATED, event);
    }

    public void publishUpdated(TeamResponse team) {
        var event = new TeamEvent.Updated(
                team.getId(),
                team.getName(),
                team.getCountry(),
                team.getCoach()

        );
        send(RoutingKeys.TEAM_UPDATED, event);
    }

    /**
     * Публикует событие «команда удалёна» с названием команды
     */
    public void publishDeleted(TeamResponse team, int matchesCount) {
        var event = new TeamEvent.Deleted(
                team.getId(),
                team.getName(),
                matchesCount
        );
        send(RoutingKeys.TEAM_DELETED, event);
    }

    private void send(String routingKey, TeamEvent event) {
        try {
            EventEnvelope<TeamEvent> envelope = EventEnvelope.wrap(event, SOURCE, routingKey);
            rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, routingKey, envelope);
            log.info("Событие отправлено: {} [eventId={}]", routingKey, envelope.metadata().eventId());
        } catch (Exception e) {
            log.error("Не удалось отправить событие {}: {}", routingKey, e.getMessage());
        }
    }
}

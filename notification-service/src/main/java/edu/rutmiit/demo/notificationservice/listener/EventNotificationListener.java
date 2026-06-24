package edu.rutmiit.demo.notificationservice.listener;

import edu.rutmiit.demo.events.*;
import edu.rutmiit.demo.notificationservice.websocket.NotificationWebSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Слушатель всех доменных событий из RabbitMQ.
 * <p>
 * Получает события из очереди q.notifications.all (binding "#"),
 * формирует человекочитаемое JSON-уведомление и рассылает
 * всем подключённым WebSocket-клиентам через NotificationWebSocketHandler.
 * <p>
 * Дедупликация — по eventId (на случай повторной доставки RabbitMQ).
 */
@Component
public class EventNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(EventNotificationListener.class);

    private final NotificationWebSocketHandler webSocketHandler;
    private final JsonMapper jsonMapper;

    /**
     * Набор обработанных eventId для дедупликации.
     */
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    public EventNotificationListener(NotificationWebSocketHandler webSocketHandler,
                                     JsonMapper jsonMapper) {
        this.webSocketHandler = webSocketHandler;
        this.jsonMapper = jsonMapper;
    }

    @RabbitListener(queues = "q.notifications.all", messageConverter = "")
    public void handleEvent(Message message) {
        try {
            byte[] body = message.getBody();
            JsonNode root = jsonMapper.readTree(body);

            // Парсим метаданные
            JsonNode metaNode = root.get("metadata");
            EventMetadata metadata = jsonMapper.treeToValue(metaNode, EventMetadata.class);

            // Дедупликация по eventId 
            if (!processedEventIds.add(metadata.eventId())) {
                log.warn("Дубликат уведомления пропущен: eventId={}", metadata.eventId());
                return;
            }

            // Формируем уведомление
            JsonNode payloadNode = root.get("payload");
            String title = buildTitle(metadata.eventType());
            String description = buildDescription(metadata.eventType(), payloadNode);
            String icon = resolveIcon(metadata.eventType());
            String level = resolveLevel(metadata.eventType());

            // JSON для WebSocket-клиента 
            String notificationJson = jsonMapper.writeValueAsString(
                    new NotificationPayload(
                            "NOTIFICATION",
                            metadata.eventId(),
                            metadata.eventType(),
                            title,
                            description,
                            icon,
                            level,
                            metadata.source(),
                            metadata.timestamp().toString(),
                            Instant.now().toString()
                    )
            );

            // Broadcast в WebSocket
            webSocketHandler.broadcast(notificationJson);

            log.info("[NOTIFY] {} | {} (клиентов: {})",
                    metadata.eventType(), description, webSocketHandler.getActiveConnectionCount());

        } catch (Exception e) {
            log.error("Ошибка обработки события для уведомлений: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось обработать событие", e);
        }
    }

    // Формирование заголовка уведомления

    private String buildTitle(String eventType) {
        return switch (eventType) {
            case "match.created" -> "Создан матч";
            case "match.goal.scored" -> "Гол!";
            case "match.status.changed" -> "Изменение статуса матча";
            case "match.enriched" -> "Аналитика матча";
            case "match.deleted" -> "Матч удалён";
            case "team.created" -> "Создана команда";
            case "team.deleted" -> "Команда удалена";
            default -> "Новое событие";
        };
    }

    private String resolveIcon(String eventType) {
        return switch (eventType) {
            case "match.created" -> "football";
            case "match.goal.scored" -> "goal";
            case "match.status.changed" -> "clock";
            case "match.enriched" -> "analytics";
            case "team.created" -> "team";
            case "team.deleted" -> "team-remove";
            default -> "bell";
        };
    }

    private String resolveLevel(String eventType) {
        return switch (eventType) {
            case "match.deleted",
                 "team.deleted" -> "warning";
            case "match.enriched" -> "info";
            default -> "success";
        };
    }

    // Формирование описания
    private String buildDescription(String eventType, JsonNode payload) {
        try {
            return switch (eventType) {
                case "match.created" -> {
                    MatchEvent.Created e =
                            jsonMapper.treeToValue(payload, MatchEvent.Created.class);
                    yield "%s - %s".formatted(
                            e.homeTeamName(),
                            e.awayTeamName()
                    );
                }
                case "match.goal.scored" -> {
                    MatchEvent.GoalScored e =
                            jsonMapper.treeToValue(payload, MatchEvent.GoalScored.class);
                    yield "Гол команды %s. Счёт %d:%d".formatted(
                            e.teamName(),
                            e.homeScore(),
                            e.awayScore()
                    );
                }
                case "match.status.changed" -> {
                    MatchEvent.StatusChanged e =
                            jsonMapper.treeToValue(payload, MatchEvent.StatusChanged.class);
                    yield "Статус изменён: %s → %s".formatted(
                            e.oldStatus(),
                            e.newStatus()
                    );
                }
                case "match.enriched" -> {
                    MatchEvent.Enriched e =
                            jsonMapper.treeToValue(payload, MatchEvent.Enriched.class);
                    yield "Лидер: %s, разница мячей: %d".formatted(
                            e.leader(),
                            e.goalDifference()
                    );
                }
                case "match.deleted" -> {
                    MatchEvent.Deleted e =
                            jsonMapper.treeToValue(payload, MatchEvent.Deleted.class);
                    yield "Удалён матч %s - %s".formatted(
                            e.homeTeamName(),
                            e.awayTeamName()
                    );
                }
                case "team.created" -> {
                    TeamEvent.Created e =
                            jsonMapper.treeToValue(payload, TeamEvent.Created.class);
                    yield "Создана команда %s".formatted(
                            e.name()
                    );
                }
                case "team.deleted" -> {
                    TeamEvent.Deleted e =
                            jsonMapper.treeToValue(payload, TeamEvent.Deleted.class);
                    yield "Удалена команда %s".formatted(
                            e.teamName()
                    );
                }
                default -> "Неизвестное событие";
            };
        } catch (Exception e) {
            return "Ошибка обработки события";
        }
    }

    /**
     * Payload уведомления для WebSocket.
     */
    record NotificationPayload(
            String type,
            String eventId,
            String eventType,
            String title,
            String description,
            String icon,
            String level,
            String source,
            String eventTimestamp,
            String receivedAt
    ) {
    }
}

package edu.rutmiit.demo.events;

/**
 * Константы для маршрутизации событий в RabbitMQ.
 * <p>
 * Routing key в topic exchange работает как почтовый индекс:
 * - "match.created" — конкретное событие
 * - "match.*"       — все события матчей
 * - "#"            — все события вообще
 * <p>
 * Вынесены в контракт, чтобы publisher и consumer использовали одни и те же строки.
 * Рассогласование routing key — частая ошибка, которую трудно отследить.
 */
public final class RoutingKeys {

    private RoutingKeys() {
    }

    public static final String EXCHANGE = "football.events";
    public static final String MATCH_CREATED = "match.created";
    public static final String MATCH_DELETED = "match.deleted";
    public static final String MATCH_ENRICHED = "match.enriched";
    public static final String MATCH_STATUS_CHANGED = "match.status.changed";
    public static final String MATCH_GOAL_SCORED = "match.goal.scored";
    public static final String TEAM_CREATED = "team.created";
    public static final String TEAM_UPDATED = "team.updated";
    public static final String TEAM_DELETED = "team.deleted";
    public static final String ALL_MATCH_EVENTS = "match.#";
    public static final String ALL_TEAM_EVENTS = "team.#";
    public static final String ALL_EVENTS = "#";
}

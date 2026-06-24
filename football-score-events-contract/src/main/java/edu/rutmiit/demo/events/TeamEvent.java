package edu.rutmiit.demo.events;

/**
 * Семейство событий, связанных с авторами.
 *
 * Аналогично MatchEvent — sealed interface гарантирует полный перечень вариантов.
 * Десериализация по eventType, а не через Jackson-аннотации.
 */
public sealed interface TeamEvent {

    /**
     * Команда создана. Содержит основные атрибуты новой команды.
     */
    record Created(
            Long teamId,
            String name,
            String country,
            String coach
    ) implements TeamEvent {}

    //Команда обновлена.

    record Updated(
            Long teamId,
            String name,
            String country,
            String coach
    ) implements TeamEvent{}

    /**
     * Команда удалёна. В нашей системе удаление каскадное — вместе с матчами.
     */
    record Deleted(
            Long teamId,
            String teamName,
            int matchesCount
    ) implements TeamEvent {}
}

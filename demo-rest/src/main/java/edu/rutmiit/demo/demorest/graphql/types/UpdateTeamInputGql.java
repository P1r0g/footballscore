package edu.rutmiit.demo.demorest.graphql.types;

import java.time.LocalDate;

/**
 * Входной тип для обновления автора.
 * Соответствует input UpdateAuthorInput в GraphQL-схеме.
 */
public record UpdateTeamInputGql(
        String name,
        String country,
        String coach
) {}

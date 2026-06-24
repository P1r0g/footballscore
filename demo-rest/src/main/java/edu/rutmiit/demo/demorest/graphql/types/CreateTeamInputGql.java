package edu.rutmiit.demo.demorest.graphql.types;

import java.time.LocalDate;

/**
 * Входной тип для создания автора.
 * Соответствует input CreateTeamInput в GraphQL-схеме.
 */
public record CreateTeamInputGql(
        String name,
        String country,
        String coach
) {}

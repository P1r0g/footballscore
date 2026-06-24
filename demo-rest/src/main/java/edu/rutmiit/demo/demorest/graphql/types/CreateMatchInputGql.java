package edu.rutmiit.demo.demorest.graphql.types;

/**
 * Входной тип для создания книги.
 * Соответствует input CreateBookInput в GraphQL-схеме.
 */
public record CreateMatchInputGql(
    String homeTeamId,
    String awayTeamId
) {}

package edu.rutmiit.demo.demorest.graphql.types;

import edu.rutmiit.demo.footballscoreapicontract.dto.MatchStatus;

/**
 * Входной тип для фильтрации книг.
 * Соответствует input BookFilter в GraphQL-схеме.
 *
 * Все поля необязательны — клиент передаёт только нужные фильтры.
 */
public record MatchFilterGql(
        String teamId,
        MatchStatus status
) {}

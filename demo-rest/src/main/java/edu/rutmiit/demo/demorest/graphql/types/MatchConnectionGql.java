package edu.rutmiit.demo.demorest.graphql.types;

import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchResponse;

import java.util.List;

/**
 * Тип-обёртка для постраничного ответа со списком книг.
 * Соответствует типу BookConnection в GraphQL-схеме.
 */
public record MatchConnectionGql(
        List<MatchResponse> content,
        PageInfoGql pageInfo,
        int totalElements
) {}

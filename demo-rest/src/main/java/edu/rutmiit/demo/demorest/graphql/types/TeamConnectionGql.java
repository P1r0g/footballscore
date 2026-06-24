package edu.rutmiit.demo.demorest.graphql.types;

import edu.rutmiit.demo.footballscoreapicontract.dto.team.TeamResponse;

import java.util.List;

/**
 * Тип-обёртка для постраничного ответа со списком команд.
 * Соответствует типу TeamConnection в GraphQL-схеме.
 */
public record TeamConnectionGql(
        List<TeamResponse> content,
        PageInfoGql pageInfo,
        int totalElements
) {}

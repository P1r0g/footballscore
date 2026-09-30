package edu.rutmiit.demo.demorest.graphql.types;

public record CreateTeamInputGql(
        String name,
        String country,
        String coach,
        String stadium
) {}
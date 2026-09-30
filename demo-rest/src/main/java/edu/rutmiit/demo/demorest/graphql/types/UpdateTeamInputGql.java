package edu.rutmiit.demo.demorest.graphql.types;

public record UpdateTeamInputGql(
        String name,
        String country,
        String coach,
        String stadium
) {}
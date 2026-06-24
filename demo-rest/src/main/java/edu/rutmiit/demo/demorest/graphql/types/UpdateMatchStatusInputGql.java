package edu.rutmiit.demo.demorest.graphql.types;

import edu.rutmiit.demo.footballscoreapicontract.dto.MatchStatus;

public record UpdateMatchStatusInputGql(
        MatchStatus status
) {
}

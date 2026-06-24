package edu.rutmiit.demo.footballscoreapicontract.validation;

import edu.rutmiit.demo.footballscoreapicontract.dto.match.MatchRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TeamsValidator
        implements ConstraintValidator<ValidTeams, MatchRequest> {

    @Override
    public boolean isValid(
            MatchRequest value,
            ConstraintValidatorContext context
    ) {

        if (value == null) {
            return true;
        }

        if (value.homeTeamId() == null ||
                value.awayTeamId() == null) {
            return true;
        }

        return !value.homeTeamId()
                .equals(value.awayTeamId());
    }
}
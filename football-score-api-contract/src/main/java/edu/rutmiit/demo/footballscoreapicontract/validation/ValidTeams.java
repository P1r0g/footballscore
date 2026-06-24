package edu.rutmiit.demo.footballscoreapicontract.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TeamsValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTeams {

    String message()
            default "Домашняя и гостевая команды должны различаться";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
package com.twittersfs.server.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueEmailConstraintValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueEmail {
    String message() default "Email must be unique";
    Class<?>[] groups() default{};
    Class<? extends Payload>[] payload() default {};
}

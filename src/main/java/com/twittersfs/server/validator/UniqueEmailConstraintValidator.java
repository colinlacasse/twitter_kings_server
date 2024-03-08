package com.twittersfs.server.validator;

import com.twittersfs.server.repos.UserEntityRepo;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueEmailConstraintValidator implements ConstraintValidator<UniqueEmail,String> {
    private final UserEntityRepo userEntityRepo;

    public UniqueEmailConstraintValidator(UserEntityRepo userEntityRepo) {
        this.userEntityRepo = userEntityRepo;
    }

    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return userEntityRepo.findByEmail(value) == null;
    }
}

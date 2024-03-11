package com.twittersfs.server.dtos.user;

import com.twittersfs.server.validator.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegister {
    @Email(message = "Email must have a valid format")
    @UniqueEmail
    private String email;
    @NotBlank(message = "Password field must not be empty")
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,20}$",
            message = "Password must have 8-20 symbols, lowercase, uppercase and symbols")
    private String password;
}

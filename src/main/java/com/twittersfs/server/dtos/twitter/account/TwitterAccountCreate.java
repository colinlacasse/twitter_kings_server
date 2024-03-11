package com.twittersfs.server.dtos.twitter.account;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwitterAccountCreate {
    @NotBlank(message = "Auth token field must not be empty")
    private String authToken;
    @NotBlank(message = "Ct0 token field must not be empty")
    private String csrfToken;
    @NotBlank(message = "Proxy field must not be empty")
    private String proxy;
    @NotBlank(message = "Twitter account username must not be empty")
    private String username;
    @NotBlank(message = "Message for sfs groups must not be empty")
    private String message;
    @NotBlank(message = "Twitter account email must not be empty")
    private String email;
    @NotBlank(message = "Twitter account password must not be empty")
    private String password;
}

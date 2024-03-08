package com.twittersfs.server.dtos.twitter.account;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwitterAccountCreate {
    @NotBlank(message = "Auth token must not be null")
    private String authToken;
    @NotBlank(message = "Ct0 token must not be null")
    private String csrfToken;
    @NotBlank(message = "Proxy must not be null")
    private String proxy;
    @NotBlank(message = "Twitter account username must not be null")
    private String username;
    @NotBlank(message = "Message for sfs groups must not be null")
    private String message;
    private String email;
}

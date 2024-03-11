package com.twittersfs.server.services.twitter.auth.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AccountCredential {
    private String username;
    private String email;
    private String password;
}

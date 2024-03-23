package com.twittersfs.server.dtos.twitter.auth.common;

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

package com.twittersfs.server.security;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AuthDto {
    private String email;
    private String password;
    private String role;
}

package com.twittersfs.server.security;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPassword {
    private String email;
    private String password;
}

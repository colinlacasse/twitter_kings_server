package com.twittersfs.server.dtos.twitter.auth.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InitLogin {
    private String flowToken;
    private String cookies;
}

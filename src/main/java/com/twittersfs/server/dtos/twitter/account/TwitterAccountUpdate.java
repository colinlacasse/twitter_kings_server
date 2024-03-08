package com.twittersfs.server.dtos.twitter.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwitterAccountUpdate {
    private String authToken;
    private String csrfToken;
    private String proxy;
    private String username;
    private String email;
}

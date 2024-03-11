package com.twittersfs.server.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TwitterApiException {
    TWITTER_ACCOUNT_LOGIN_EXCEPTION("Login failed"),
    TWITTER_ACCOUNT_CONNECT_EXCEPTION("Unable to login twitter account"),
    TWITTER_ACCOUNT_AUTH_EXCEPTION("Twitter auth token is invalid"),
    TWITTER_ACCOUNT_PROXY_EXCEPTION("Proxy is not working"),
    TWITTER_ACCOUNT_LOCKED_EXCEPTION("Twitter account is locked"),
    TWITTER_ACCOUNT_SUSPENDED_EXCEPTION("Twitter account suspended"),
    TWITTER_ACCOUNT_COOLDOWN_EXCEPTION("Twitter account cooldown");

    private final String value;
}

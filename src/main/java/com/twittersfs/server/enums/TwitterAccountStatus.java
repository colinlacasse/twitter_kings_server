package com.twittersfs.server.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TwitterAccountStatus {
    ALL("All"),
    STOPPING("Stopping"),
    ACTIVE("Active"),
    DISABLED("Disabled"),
    LOCKED("Locked"),
    INVALID_COOKIES("Invalid cookies"),
    UNABLE_CONNECT("Unable to connect"),
    UNEXPECTED_ERROR("Unexpected error"),
    SUSPENDED("Suspended"),
    PROXY_ERROR("Proxy error"),
    COOLDOWN("Cooldown");
    private final String value;
}

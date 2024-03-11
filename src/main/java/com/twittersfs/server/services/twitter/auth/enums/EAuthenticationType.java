package com.twittersfs.server.services.twitter.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EAuthenticationType {
    GUEST("GUEST"),
    USER("USER"),
    LOGIN("LOGIN");

    private final String value;
}

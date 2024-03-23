package com.twittersfs.server.enums;

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

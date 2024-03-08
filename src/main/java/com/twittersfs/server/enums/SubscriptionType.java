package com.twittersfs.server.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SubscriptionType {
    BASIC("basic"),
    PREMIUM("premium");
    private final String value;
}

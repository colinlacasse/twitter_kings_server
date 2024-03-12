package com.twittersfs.server.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
@Getter
@RequiredArgsConstructor
public enum SubscriptionType {
    BASIC("basic"),
    DONOR("donor"),
    AGENCY("agency"),
    PREMIUM("premium");
    private final String value;
}

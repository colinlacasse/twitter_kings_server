package com.twittersfs.server.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaidStatus {
    PAID("true"),
    NOT_PAID("false");
    private final String value;
}

package com.twittersfs.server.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Prices {
    X_MONTH_SUBSCRIPTION(10),
    X_2MONTH_SUBSCRIPTION(20),
    X_3MONTH_SUBSCRIPTION(30),
    X_6MONTH_SUBSCRIPTION(60),
    X_12MONTH_SUBSCRIPTION(120);
    private final Integer value;
}

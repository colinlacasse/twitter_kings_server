package com.twittersfs.server.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Prices {
    X_DAY_SUBSCRIPTION(0.96F),
    X_MONTH_SUBSCRIPTION(29F),
    X_2MONTH_SUBSCRIPTION(58F),
    X_3MONTH_SUBSCRIPTION(87F),
    X_6MONTH_SUBSCRIPTION(174F),
    X_12MONTH_SUBSCRIPTION(348F);
    private final Float value;
}

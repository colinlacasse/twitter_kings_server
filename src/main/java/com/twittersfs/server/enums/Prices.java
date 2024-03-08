package com.twittersfs.server.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Prices {
    X_DAY_SUBSCRIPTION(0.33F),
    X_MONTH_SUBSCRIPTION(10F),
    X_2MONTH_SUBSCRIPTION(20F),
    X_3MONTH_SUBSCRIPTION(30F),
    X_6MONTH_SUBSCRIPTION(60F),
    X_12MONTH_SUBSCRIPTION(120F);
    private final Float value;
}

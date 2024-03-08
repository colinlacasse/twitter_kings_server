package com.twittersfs.server.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgencyPaidStatus {
    PAID("paid"),
    UNPAID("unpaid");
    private final String value;
}

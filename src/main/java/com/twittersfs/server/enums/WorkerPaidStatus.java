package com.twittersfs.server.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
@Getter
@RequiredArgsConstructor
public enum WorkerPaidStatus {
    PAID("paid"),
    UNPAID("unpaid");
    private final String value;
}

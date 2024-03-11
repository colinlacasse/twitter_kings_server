package com.twittersfs.server.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProxyType {
    HTTP("http"),
    SOCKS("socks");

    private final String value;
}

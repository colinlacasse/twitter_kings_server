package com.twittersfs.server.exceptions.twitter;

public class XAccountRateLimitException extends RuntimeException {
    public XAccountRateLimitException(String message) {
        super(message);
    }
}

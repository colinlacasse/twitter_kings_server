package com.twittersfs.server.exceptions.twitter;

public class XAccountOverCapacityException extends RuntimeException {
    public XAccountOverCapacityException(String message) {
        super(message);
    }
}

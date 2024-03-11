package com.twittersfs.server.exceptions.twitter;

import com.twittersfs.server.enums.TwitterApiException;

public class XAccountLockedException extends RuntimeException{
    public XAccountLockedException(String message) {
        super(TwitterApiException.TWITTER_ACCOUNT_LOCKED_EXCEPTION.getValue());
    }
}

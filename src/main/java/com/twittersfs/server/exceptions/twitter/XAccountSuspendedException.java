package com.twittersfs.server.exceptions.twitter;

import com.twittersfs.server.enums.TwitterApiException;

public class XAccountSuspendedException extends RuntimeException{
    public XAccountSuspendedException(String message){
        super(TwitterApiException.TWITTER_ACCOUNT_SUSPENDED_EXCEPTION.getValue());
    }
}

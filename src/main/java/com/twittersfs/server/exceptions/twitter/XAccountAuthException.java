package com.twittersfs.server.exceptions.twitter;

import com.twittersfs.server.enums.TwitterApiException;

public class XAccountAuthException extends RuntimeException{
    public XAccountAuthException(String message){
        super(TwitterApiException.TWITTER_ACCOUNT_AUTH_EXCEPTION.getValue());
    }
}

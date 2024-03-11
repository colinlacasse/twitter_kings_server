package com.twittersfs.server.exceptions.twitter;

import com.twittersfs.server.enums.TwitterApiException;

public class XAccountConnectException extends RuntimeException{
    public XAccountConnectException(String message){
        super(TwitterApiException.TWITTER_ACCOUNT_CONNECT_EXCEPTION.getValue());
    }
}

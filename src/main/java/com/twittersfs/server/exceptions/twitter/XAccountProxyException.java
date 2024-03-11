package com.twittersfs.server.exceptions.twitter;

import com.twittersfs.server.enums.TwitterApiException;

public class XAccountProxyException extends RuntimeException{
    public XAccountProxyException(String message){
        super(TwitterApiException.TWITTER_ACCOUNT_PROXY_EXCEPTION.getValue());
    }
}

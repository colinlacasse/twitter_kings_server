package com.twittersfs.server.exceptions.twitter;

import com.twittersfs.server.enums.TwitterApiException;

public class XAccountCooldownException extends RuntimeException{
    public XAccountCooldownException(String message){
        super(TwitterApiException.TWITTER_ACCOUNT_COOLDOWN_EXCEPTION.getValue());
    }
}

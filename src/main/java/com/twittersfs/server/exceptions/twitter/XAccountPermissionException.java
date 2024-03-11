package com.twittersfs.server.exceptions.twitter;

public class XAccountPermissionException extends RuntimeException{
    public XAccountPermissionException(String message){
        super(message);
    }
}

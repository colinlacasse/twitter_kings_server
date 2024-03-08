package com.twittersfs.server.exceptions.user;

public class NotEnoughFunds extends RuntimeException{
    public NotEnoughFunds(String message){
        super(message);
    }
}

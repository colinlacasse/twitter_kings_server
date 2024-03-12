package com.twittersfs.server.services.twitter.app.commands;

public interface TwitterCommandsService {
    void execute(Long twitterAccountId) throws InterruptedException;
}

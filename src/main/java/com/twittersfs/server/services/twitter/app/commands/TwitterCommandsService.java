package com.twittersfs.server.services.twitter.app.commands;

import java.util.Set;

public interface TwitterCommandsService {
    void execute(Long twitterAccountId) throws InterruptedException;
    void stop(Long twitterAccountId);
    void checkIfAccountRunning(Long accountId);
}

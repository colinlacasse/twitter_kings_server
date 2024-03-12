package com.twittersfs.server.services.twitter.app;

public interface TwitterAppService {
    void run(Long twitterAccountId);
    void stop(Long twitterAccountId);
    void addGroups(Long twitterAccountId);
}

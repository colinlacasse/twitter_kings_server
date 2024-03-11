package com.twittersfs.server.services.twitter.auth;

import com.twittersfs.server.entities.TwitterAccount;

import java.io.IOException;

public interface TwitterAuthService {
//    void login(TwitterAccount twitterAccount) throws IOException;
    void login(Long twitterAccountId) throws IOException;
}

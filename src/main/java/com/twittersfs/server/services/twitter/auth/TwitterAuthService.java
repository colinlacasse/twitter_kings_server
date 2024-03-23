package com.twittersfs.server.services.twitter.auth;

import com.twittersfs.server.entities.TwitterAccount;

import java.io.IOException;

public interface TwitterAuthService {
    void login(TwitterAccount twitterAccount) ;
    void unlock(TwitterAccount twitterAccount) ;
//    void newLogin(TwitterAccount twitterAccount) throws IOException;
}

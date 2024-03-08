package com.twittersfs.server.services;

import com.twittersfs.server.dtos.twitter.account.TwitterAccountCreate;
import com.twittersfs.server.dtos.twitter.account.TwitterAccountUpdate;
import com.twittersfs.server.enums.TwitterAccountStatus;

import java.net.UnknownHostException;
import java.util.List;

public interface TwitterAccountService {

    void createTwitterAccount(String email, Long modelId, TwitterAccountCreate dto) throws UnknownHostException;
    void createTwitterAccountBulk(String email, Long modelId, List<TwitterAccountCreate> dtos);
    void updateTwitterAccountStatus(Long twitterAccountId, TwitterAccountStatus status);
    void updateTwitterAccount(Long twitterAccountId, TwitterAccountUpdate dto) throws UnknownHostException;
    void deleteTwitterAccount(Long twitterAccountId);
    void deleteProxyFromTwitterAccount(Long twitterAccountId);
}

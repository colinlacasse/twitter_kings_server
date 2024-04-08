package com.twittersfs.server.services;

import com.twittersfs.server.dtos.common.PageableResponse;
import com.twittersfs.server.dtos.twitter.account.TwitterAccountCreate;
import com.twittersfs.server.dtos.twitter.account.TwitterAccountData;
import com.twittersfs.server.dtos.twitter.account.TwitterAccountUpdate;
import com.twittersfs.server.dtos.twitter.message.TwitterChatMessageDto;
import com.twittersfs.server.dtos.twitter.statistic.XAccountStatistic;
import com.twittersfs.server.dtos.twitter.statistic.XStatistic;
import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.enums.TwitterAccountStatus;

import java.net.UnknownHostException;
import java.util.List;

public interface TwitterAccountService {

    void createTwitterAccount(String email, Long modelId, TwitterAccountCreate dto) throws UnknownHostException;
    void createTwitterAccountBulk(String email, Long modelId, List<TwitterAccountCreate> dtos);
    void updateTwitterAccountStatus(Long twitterAccountId, TwitterAccountStatus status);
    void updateSentMessages(Long twitterAccountId);
    void updateTwitterAccount(Long twitterAccountId, TwitterAccountUpdate dto) throws UnknownHostException;
    void deleteTwitterAccount(Long twitterAccountId);
    void deleteProxyFromTwitterAccount(Long twitterAccountId);
    void updateSubscription(Long twitterAccountId, Integer month);
    void addChatMessage(Long twitterAccountId, TwitterChatMessageDto dto);
    void deleteChatMessage(Long messageId);
    TwitterAccount get(Long twitterAccountId);
    void updateRestId(Long twitterAccountId, String restId);
    void updateGroups(Long twitterAccountId, Integer groups);
    void updateStatisticDifference(Long twitterAccountId, Integer friendDifference, Integer messageDifference, Integer retweetDifference, Integer friends, Integer retweets);
    void setMessagesDifferenceViewed(Long twitterAccountId);
    void setRetweetsDifferenceViewed(Long twitterAccountId);
    void setFriendsDifferenceViewed(Long twitterAccountId);
    List<TwitterAccount> findAll();
    PageableResponse<TwitterAccountData> getFilteredTwitterAccounts(String email, TwitterAccountStatus status, int page, int size);
    PageableResponse<TwitterAccountData> getTwitterAccountsByModel(Long modelId, int page, int size);
    XStatistic getAccountStatistic(Long twitterAccountId);
}

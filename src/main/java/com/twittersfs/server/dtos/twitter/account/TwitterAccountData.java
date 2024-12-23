package com.twittersfs.server.dtos.twitter.account;

import com.twittersfs.server.dtos.twitter.message.TwitterChatMessageData;
import com.twittersfs.server.enums.TwitterAccountStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@Builder
public class TwitterAccountData {
    private Long id;
    private String username;
    private String model;
    private TwitterAccountStatus status;
    private String paidTo;
    private String proxy;
    private String email;
    private String password;
    private Integer speed;
    private String auth;
    private String ct0;
    private Integer groups;
    private List<TwitterChatMessageData> chatMessages;
    private Integer messages;
    private Integer friends;
    private Integer retweets;
    private Integer friendsDifference;
    private Integer retweetsDifference;
    private Integer messagesDifference;
}

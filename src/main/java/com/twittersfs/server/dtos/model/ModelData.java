package com.twittersfs.server.dtos.model;

import com.twittersfs.server.dtos.twitter.account.TwitterAccountData;
import lombok.Builder;

import java.util.List;

@Builder
public class ModelData {
    private Long id;
    private String name;
    private List<TwitterAccountData> twitterAccounts;
}

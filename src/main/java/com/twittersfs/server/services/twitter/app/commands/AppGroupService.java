package com.twittersfs.server.services.twitter.app.commands;

import com.twittersfs.server.entities.TwitterAccount;

public interface AppGroupService {
    void addGroupsToADonorAccount(TwitterAccount toUpdate, String restId);
}

package com.twittersfs.server.services.twitter.app.commands;

import com.twittersfs.server.dtos.twitter.group.Conversation;
import com.twittersfs.server.dtos.twitter.group.XUserGroup;
import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.enums.GroupStatus;
import com.twittersfs.server.enums.SubscriptionType;
import com.twittersfs.server.repos.TwitterAccountRepo;
import com.twittersfs.server.services.twitter.readonly.TwitterApiRequests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.nonNull;

@Service
@Slf4j
public class AppGroupServiceImpl implements AppGroupService {

    private final TwitterApiRequests twitterApiRequests;
    private final TwitterAccountRepo twitterAccountRepo;


    public AppGroupServiceImpl(TwitterApiRequests twitterApiRequests, TwitterAccountRepo twitterAccountRepo) {
        this.twitterApiRequests = twitterApiRequests;
        this.twitterAccountRepo = twitterAccountRepo;
    }

    @Override
    public void addGroupsToADonorAccount(TwitterAccount receiver, String receiverRestId) {
        List<TwitterAccount> all = twitterAccountRepo.findAll();
        List<TwitterAccount> donors = new ArrayList<>();
        Collections.shuffle(donors);
        for (TwitterAccount account : all) {
            SubscriptionType subscriptionType = account.getModel().getUser().getSubscriptionType();
            if (subscriptionType.equals(SubscriptionType.BASIC)) {
                if (account.getGroupStatus().equals(GroupStatus.UNUSED)) {
                    donors.add(account);
                }
            }
        }
        int groupsBeforeAdding = receiver.getGroups();
        for (TwitterAccount account : donors) {
            try {
                addGroups(account, receiver, receiverRestId);
                XUserGroup afterAdding = twitterApiRequests.getUserConversations(receiver.getUsername(), receiver.getRestId(), receiver.getProxy(), receiver.getCookie(), receiver.getAuthToken(), receiver.getCsrfToken());
                int groupsAfterAdding = 0;
                for (Conversation conversation : afterAdding.getInboxInitialState().getConversations().values()) {
                    if (nonNull(conversation.getName())) {
                        groupsAfterAdding++;
                    }
                }
                if (groupsAfterAdding - groupsBeforeAdding > 15) {
                    twitterAccountRepo.updateGroupStatus(account.getId(), GroupStatus.USED);
                }
            } catch (Exception e) {
                log.error("Error while adding groups to a donor account : " + e);
            }
        }
    }

    private void addGroups(TwitterAccount donor, TwitterAccount receiver, String receiverRestId) throws IOException {
        twitterApiRequests.setDmSettings(donor);
        twitterApiRequests.subscribeOnAccount(donor, receiverRestId);
        twitterApiRequests.subscribeOnAccount(receiver, donor.getRestId());
        XUserGroup groups = twitterApiRequests.getUserConversations(donor.getUsername(), donor.getRestId(), donor.getProxy(), donor.getCookie(), donor.getAuthToken(), donor.getCsrfToken());
        for (Conversation conversation : groups.getInboxInitialState().getConversations().values()) {
            if (nonNull(conversation.getName())) {
                try {
                    addGroup(receiverRestId, donor, conversation.getConversationID());
                } catch (Exception e) {
                    log.error("Error while adding group to a donor account : " + e);
                }
            }
        }
    }

    private void addGroup(String toRestId, TwitterAccount from, String groupId) throws IOException {
        twitterApiRequests.addGroupToAccount(from, toRestId, groupId);
    }
}

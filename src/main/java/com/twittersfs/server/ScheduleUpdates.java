package com.twittersfs.server;

import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.enums.GroupStatus;
import com.twittersfs.server.enums.SubscriptionType;
import com.twittersfs.server.enums.TwitterAccountStatus;
import com.twittersfs.server.repos.TwitterAccountRepo;
import com.twittersfs.server.services.twitter.app.TwitterAppService;
import com.twittersfs.server.services.twitter.app.commands.AppGroupService;
import com.twittersfs.server.services.twitter.auth.TwitterAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ScheduleUpdates {
    private final TwitterAccountRepo twitterAccountRepo;
    private final TwitterAppService twitterAppService;
    private final TwitterAuthService authService;
    private final AppGroupService appGroupService;

    public ScheduleUpdates(TwitterAccountRepo twitterAccountRepo, TwitterAppService twitterAppService, TwitterAuthService authService, AppGroupService appGroupService) {
        this.twitterAccountRepo = twitterAccountRepo;
        this.twitterAppService = twitterAppService;
        this.authService = authService;
        this.appGroupService = appGroupService;
    }

//    @Scheduled(fixedRate = 14400000)
//    public void updateCookiesAndRestart() {
//        List<TwitterAccount> all = twitterAccountRepo.findAll();
//        List<TwitterAccount> needUpdate = new ArrayList<>();
//        for (TwitterAccount account : all) {
//            if (account.getStatus().equals(TwitterAccountStatus.INVALID_COOKIES)) {
//                needUpdate.add(account);
//            }
//        }
//
//        for (TwitterAccount account : needUpdate) {
//            try {
//                authService.login(account);
//                TwitterAccount updated = twitterAccountRepo.findById(account.getId())
//                        .orElseThrow(() -> new RuntimeException("Twitter account wish such Id does not exist"));
//                if (updated.getStatus().equals(TwitterAccountStatus.UPDATED_COOKIES)) {
//                    twitterAppService.run(updated.getId());
//                }
//            } catch (Exception e) {
//                log.error("Error while restarting : " + e);
//            }
//        }
//    }

    @Scheduled(fixedRate = 86400000)
    public void updatedGroups() {
        List<TwitterAccount> all = twitterAccountRepo.findAll();
        for (TwitterAccount account : all) {
            SubscriptionType subscriptionType = account.getModel().getUser().getSubscriptionType();
            if (subscriptionType.equals(SubscriptionType.AGENCY)) {
                if (account.getGroups() < 10) {
                    appGroupService.addGroupsToAgencyAccount(account, account.getRestId());
                }
            }
        }
    }
}

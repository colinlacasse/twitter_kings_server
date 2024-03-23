package com.twittersfs.server;

import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.enums.GroupStatus;
import com.twittersfs.server.enums.SubscriptionType;
import com.twittersfs.server.enums.TwitterAccountStatus;
import com.twittersfs.server.repos.TwitterAccountRepo;
import com.twittersfs.server.services.twitter.app.TwitterAppService;
import com.twittersfs.server.services.twitter.app.commands.AppGroupService;
import com.twittersfs.server.services.twitter.auth.TwitterAuthService;
import jakarta.annotation.PostConstruct;
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
    private boolean firstRun = false;
    private final AppGroupService appGroupService;

    public ScheduleUpdates(TwitterAccountRepo twitterAccountRepo, TwitterAppService twitterAppService, TwitterAuthService authService, AppGroupService appGroupService) {
        this.twitterAccountRepo = twitterAccountRepo;
        this.twitterAppService = twitterAppService;
        this.authService = authService;
        this.appGroupService = appGroupService;
    }
//    @PostConstruct
//    public void init() {
//        firstRun = true;
//    }
//
//    @Scheduled(fixedRate = 10800000)
//    public void updateCookiesAndRestart() {
//        if (firstRun) {
//            try {
//                Thread.sleep(300000);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//            firstRun = false;
//        }
//
//        log.info("!!!!!!!!!!!!!!!!!!Updating cookies started!!!!!!!!!!!!!!!!!!!!");
//        List<TwitterAccount> all = twitterAccountRepo.findAll();
//        for (TwitterAccount account : all) {
//            TwitterAccount twitterAccount = twitterAccountRepo.findById(account.getId()).orElseThrow(()-> new RuntimeException("No account with such id"));
//            TwitterAccountStatus status = twitterAccount.getStatus();
//            if (status.equals(TwitterAccountStatus.INVALID_COOKIES) || status.equals(TwitterAccountStatus.UNEXPECTED_ERROR)) {
//                try {
//                    authService.login(account);
//                    twitterAppService.run(account.getId());
//                } catch (Exception e) {
//                    log.error("Error while restarting : " + e + " account : " + account.getUsername());
//                }
//            }
//        }
//    }

//    @Scheduled(fixedRate = 86400000)
//    public void updatedGroups() {
//        List<TwitterAccount> all = twitterAccountRepo.findAll();
//        for (TwitterAccount account : all) {
//            SubscriptionType subscriptionType = account.getModel().getUser().getSubscriptionType();
//            if (subscriptionType.equals(SubscriptionType.AGENCY)) {
//                if (account.getGroups() < 10) {
//                    appGroupService.addGroupsToAgencyAccount(account, account.getRestId());
//                }
//            }
//        }
//    }
}

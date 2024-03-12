package com.twittersfs.server;

import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.entities.UserEntity;
import com.twittersfs.server.enums.SubscriptionType;
import com.twittersfs.server.enums.TwitterAccountStatus;
import com.twittersfs.server.repos.TwitterAccountRepo;
import com.twittersfs.server.services.twitter.app.TwitterAppService;
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

    public ScheduleUpdates(TwitterAccountRepo twitterAccountRepo, TwitterAppService twitterAppService, TwitterAuthService authService) {
        this.twitterAccountRepo = twitterAccountRepo;
        this.twitterAppService = twitterAppService;
        this.authService = authService;
    }

    @Scheduled(fixedRate = 10800000)
    public void updateCookiesAndRestart() {
        List<TwitterAccount> all = twitterAccountRepo.findAll();
        List<TwitterAccount> needUpdate = new ArrayList<>();
        for (TwitterAccount account : all) {
            UserEntity user = account.getModel().getUser();
            if (account.getStatus().equals(TwitterAccountStatus.INVALID_COOKIES) && user.getSubscriptionType().equals(SubscriptionType.PREMIUM)) {
                needUpdate.add(account);
            }
        }

        for (TwitterAccount account : needUpdate) {
            try {
                authService.login(account);
                TwitterAccount updated = twitterAccountRepo.findById(account.getId())
                        .orElseThrow(() -> new RuntimeException("Twitter account wish such Id does not exist"));
                if (updated.getStatus().equals(TwitterAccountStatus.UPDATED_COOKIES)) {
                    twitterAppService.run(updated.getId());
                }
            } catch (Exception e) {
                log.error("Error while restarting : " + e);
            }
        }
    }
}

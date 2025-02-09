package com.twittersfs.server;

import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.enums.TwitterAccountStatus;
import com.twittersfs.server.repos.TwitterAccountRepo;
import com.twittersfs.server.services.twitter.app.TwitterAppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class LoadDataOnStartUp {
    private final TwitterAccountRepo twitterAccountRepo;
    private final TwitterAppService twitterAppService;

    public LoadDataOnStartUp(TwitterAccountRepo twitterAccountRepo, TwitterAppService twitterAppService) {
        this.twitterAccountRepo = twitterAccountRepo;
        this.twitterAppService = twitterAppService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadData(){
        List<TwitterAccount> accounts = twitterAccountRepo.findAll();
        for (TwitterAccount account : accounts) {
            TwitterAccountStatus status = account.getStatus();
            if (status.equals(TwitterAccountStatus.ACTIVE) || status.equals(TwitterAccountStatus.COOLDOWN) || status.equals(TwitterAccountStatus.UNEXPECTED_ERROR)) {
                twitterAppService.run(account.getId());
            } else if (status.equals(TwitterAccountStatus.STOPPING)) {
                twitterAccountRepo.updateStatus(account.getId(), TwitterAccountStatus.DISABLED);
            }
        }
    }
}

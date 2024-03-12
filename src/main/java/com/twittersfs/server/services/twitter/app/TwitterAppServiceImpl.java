package com.twittersfs.server.services.twitter.app;

import com.twittersfs.server.enums.TwitterAccountStatus;
import com.twittersfs.server.services.TwitterAccountService;
import com.twittersfs.server.services.twitter.app.commands.TwitterCommandsService;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TwitterAppServiceImpl implements TwitterAppService {
    ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final TwitterCommandsService commandsService;
    private final TwitterAccountService twitterAccountService;

    public TwitterAppServiceImpl(TwitterCommandsService commandsService, TwitterAccountService twitterAccountService) {
        this.commandsService = commandsService;
        this.twitterAccountService = twitterAccountService;
    }

    @Override
    public void run(Long twitterAccountId) {
        twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.ACTIVE);
        Runnable start = () -> {
            try {
                commandsService.execute(twitterAccountId);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };
        virtualExecutor.execute(start);
    }

    @Override
    public void stop(Long twitterAccountId) {
        commandsService.stop(twitterAccountId);
    }

    @Override
    public void addGroups(Long twitterAccountId) {

    }
}

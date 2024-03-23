package com.twittersfs.server.controllers;

import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.repos.TwitterAccountRepo;
import com.twittersfs.server.services.twitter.app.TwitterAppService;
import com.twittersfs.server.services.twitter.auth.TwitterAuthService;
import com.twittersfs.server.services.twitter.readonly.TwitterApiRequests;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("api/v1/app")
public class TwitterAppController {
    private final TwitterAppService twitterAppService;
    private final TwitterAuthService authService;
    private final TwitterAccountRepo accountRepo;
    private final TwitterApiRequests apiRequests;

    public TwitterAppController(TwitterAppService twitterAppService, TwitterAuthService authService, TwitterAccountRepo accountRepo, TwitterApiRequests apiRequests) {
        this.twitterAppService = twitterAppService;
        this.authService = authService;
        this.accountRepo = accountRepo;
        this.apiRequests = apiRequests;
    }

    @PostMapping("/{twitterAccountId}/run")
    public void run(@PathVariable Long twitterAccountId) {
        twitterAppService.run(twitterAccountId);
    }

    @PostMapping("/{twitterAccountId}/stop")
    public void stop(@PathVariable Long twitterAccountId) {
        twitterAppService.stop(twitterAccountId);
    }
//    @PostMapping("/{twitterAccountId}/relogin")
//    public void relogin(@PathVariable Long twitterAccountId) {
//        TwitterAccount account = accountRepo.findById(twitterAccountId).orElseThrow(() -> new RuntimeException("No Acc"));
//        authService.login(account);
//    }
    @PostMapping("/{twitterAccountId}/unlock")
    public void unlock(@PathVariable Long twitterAccountId){
        TwitterAccount account = accountRepo.findById(twitterAccountId).orElseThrow(() -> new RuntimeException("No Acc"));
        authService.unlock(account);
    }

    @PostMapping("/{twitterAccountId}/guest")
    public void guest(@PathVariable Long twitterAccountId) throws IOException {
        TwitterAccount account = accountRepo.findById(twitterAccountId).orElseThrow(() -> new RuntimeException("No Acc"));
        authService.login(account);
    }
}

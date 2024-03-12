package com.twittersfs.server.controllers;

import com.twittersfs.server.services.twitter.app.TwitterAppService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/app")
public class TwitterAppController {
    private final TwitterAppService twitterAppService;

    public TwitterAppController(TwitterAppService twitterAppService) {
        this.twitterAppService = twitterAppService;
    }

    @PostMapping("/{twitterAccountId}/run")
    public void run(@PathVariable Long twitterAccountId) {
        twitterAppService.run(twitterAccountId);
    }

    @PostMapping("/{twitterAccountId}/stop")
    public void stop(@PathVariable Long twitterAccountId) {
        twitterAppService.stop(twitterAccountId);
    }
}

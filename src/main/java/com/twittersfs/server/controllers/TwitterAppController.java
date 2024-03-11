package com.twittersfs.server.controllers;

import com.twittersfs.server.services.twitter.auth.TwitterAuthService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("api/v1/app")
public class TwitterAppController {
    private final TwitterAuthService authService;

    public TwitterAppController(TwitterAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/{twitterAccountId}/run")
    public void run(@PathVariable Long twitterAccountId) {
    }

    @PostMapping("/{twitterAccountId}/stop")
    public void stop(@PathVariable Long twitterAccountId) {
    }

    @PostMapping("/{twitterAccountId}/relogin")
    public void relogin(@PathVariable Long twitterAccountId) throws IOException {
        authService.login(twitterAccountId);
    }
}

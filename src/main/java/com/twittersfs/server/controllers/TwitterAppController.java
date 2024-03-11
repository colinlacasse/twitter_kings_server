package com.twittersfs.server.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/app")
public class TwitterAppController {
    @PostMapping("/{twitterAccountId}/run")
    public void run(@PathVariable Long twitterAccountId) {
    }

    @PostMapping("/{twitterAccountId}/stop")
    public void stop(@PathVariable Long twitterAccountId) {
    }
}

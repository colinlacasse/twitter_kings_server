package com.twittersfs.server.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ELoginUrls {
    GUEST_TOKEN("https://api.twitter.com/1.1/guest/activate.json"),
    INITIATE_LOGIN("https://api.twitter.com/1.1/onboarding/task.json?flow_name=login"),
    LOGIN_SUBTASK("https://api.twitter.com/1.1/onboarding/task.json");

    private final String value;
}

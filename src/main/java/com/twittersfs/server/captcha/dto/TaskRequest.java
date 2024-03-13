package com.twittersfs.server.captcha.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequest {
    private String clientKey;
    private Task task;
}

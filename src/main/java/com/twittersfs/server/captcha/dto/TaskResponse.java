package com.twittersfs.server.captcha.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskResponse {
    private int errorId;
    private String errorCode;
    private String errorDescription;
    private String taskId;
}

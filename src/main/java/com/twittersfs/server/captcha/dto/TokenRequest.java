package com.twittersfs.server.captcha.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRequest {
    private String clientKey;
    private String taskId;
}

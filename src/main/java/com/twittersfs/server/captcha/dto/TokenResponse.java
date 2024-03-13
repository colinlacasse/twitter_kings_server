package com.twittersfs.server.captcha.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenResponse {
    @JsonProperty("errorId")
    private int errorId;

    @JsonProperty("taskId")
    private String taskId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("solution")
    private Solution solution;
}

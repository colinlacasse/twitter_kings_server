package com.twittersfs.server.captcha.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenResponse {
    @JsonProperty("errorId")
    private int errorId;

//    @JsonProperty("taskId")
//    private String taskId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("solution")
    private Solution solution;
}

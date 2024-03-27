package com.twittersfs.server.captcha.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Solution {
    @JsonProperty("token")
    private String token;
    @JsonProperty("userAgent")
    private String userAgent;
}

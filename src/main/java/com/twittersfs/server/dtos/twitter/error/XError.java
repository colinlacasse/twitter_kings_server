package com.twittersfs.server.dtos.twitter.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XError {
    @JsonProperty("code")
    private Integer code;
    @JsonProperty("message")
    private String message;
}

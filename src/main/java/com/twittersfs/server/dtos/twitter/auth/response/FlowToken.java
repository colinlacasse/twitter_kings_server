package com.twittersfs.server.dtos.twitter.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlowToken {
    @JsonProperty("flow_token")
    private String flowToken;
}

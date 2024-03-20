package com.twittersfs.server.dtos.twitter.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XUser {
    @JsonProperty("id")
    private Long restId;
    @JsonProperty("id_str")
    private String userId;
    @JsonProperty("screen_name")
    private String screenName;
}

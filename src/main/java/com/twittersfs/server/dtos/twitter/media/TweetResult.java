package com.twittersfs.server.dtos.twitter.media;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TweetResult {
    @JsonProperty("__typename")
    private String typename;

    @JsonProperty("rest_id")
    private String restId;
    @JsonProperty("legacy")
    private Legacy legacy;
}

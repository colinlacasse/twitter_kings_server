package com.twittersfs.server.dtos.twitter.media;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Result {
    @JsonProperty("timeline_v2")
    private TimelineV2 timelineV2;
    @JsonProperty("rest_id")
    private String restId;
    @JsonProperty("legacy")
    private Legacy legacy;
}

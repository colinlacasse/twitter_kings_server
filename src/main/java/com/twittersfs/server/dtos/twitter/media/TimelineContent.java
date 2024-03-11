package com.twittersfs.server.dtos.twitter.media;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimelineContent {
    @JsonProperty("entryType")
    private String entryType;
    @JsonProperty("itemContent")
    private ItemContent itemContent;
}

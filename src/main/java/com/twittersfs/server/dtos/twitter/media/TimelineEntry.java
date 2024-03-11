package com.twittersfs.server.dtos.twitter.media;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimelineEntry {
    @JsonProperty("entryId")
    private String entryId;

    @JsonProperty("sortIndex")
    private String sortIndex;

    @JsonProperty("content")
    private TimelineContent content;
}

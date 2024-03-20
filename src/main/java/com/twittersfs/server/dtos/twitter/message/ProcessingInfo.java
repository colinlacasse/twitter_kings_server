package com.twittersfs.server.dtos.twitter.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessingInfo {
    @JsonProperty("state")
    private String state;

    @JsonProperty("progress_percent")
    private int progressPercent;
}

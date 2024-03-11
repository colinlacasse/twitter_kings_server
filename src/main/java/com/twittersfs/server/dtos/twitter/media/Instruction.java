package com.twittersfs.server.dtos.twitter.media;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Instruction {
    @JsonProperty("moduleItems")
    private List<ModuleItem> moduleItems;

    @JsonProperty("entry")
    private TimelineEntry entry;

    @JsonProperty("entries")
    private List<TimelineEntry> entries;
}

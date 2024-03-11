package com.twittersfs.server.dtos.twitter.media;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModuleItem {
    @JsonProperty("entryId")
    private String entryId;

    @JsonProperty("item")
    private Item item;
}

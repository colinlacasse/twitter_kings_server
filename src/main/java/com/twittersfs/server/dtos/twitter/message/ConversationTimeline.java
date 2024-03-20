package com.twittersfs.server.dtos.twitter.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ConversationTimeline {
    @JsonProperty("entries")
    private List<Entry> entries;
    @JsonProperty("users")
    private Map<String, XUser> users;
}

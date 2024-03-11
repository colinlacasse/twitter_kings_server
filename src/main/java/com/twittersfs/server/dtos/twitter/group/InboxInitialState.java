package com.twittersfs.server.dtos.twitter.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class InboxInitialState {
    @JsonProperty("conversations")
    private Map<String, Conversation> conversations;
}

package com.twittersfs.server.dtos.twitter.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Conversation {
    @JsonProperty("conversation_id")
    private String conversationID;
    @JsonProperty("name")
    private String name;
}

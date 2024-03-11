package com.twittersfs.server.dtos.twitter.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XGroupMessage {
    @JsonProperty("conversation_timeline")
    private ConversationTimeline conversationTimeline;
}

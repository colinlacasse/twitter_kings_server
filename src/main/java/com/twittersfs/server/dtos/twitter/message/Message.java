package com.twittersfs.server.dtos.twitter.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {
    @JsonProperty("id")
    private String id;
    @JsonProperty("time")
    private String time;
    @JsonProperty("conversation_id")
    private String conversationId;
    @JsonProperty("message_data")
    private MessageData messageData;
}

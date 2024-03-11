package com.twittersfs.server.dtos.twitter.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageData {
    @JsonProperty("sender_id")
    private String senderId;
    @JsonProperty("text")
    private String text;
    @JsonProperty("time")
    private String time;
}

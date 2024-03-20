package com.twittersfs.server.dtos.twitter.message;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TwitterChatMessageData {
    private Long id;
    private String text;
    private String gifUrl;
}

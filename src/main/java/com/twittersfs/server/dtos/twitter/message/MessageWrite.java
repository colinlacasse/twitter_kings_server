package com.twittersfs.server.dtos.twitter.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageWrite {
    @JsonProperty("conversation_id")
    private String conversation_id;
    @JsonProperty("recipient_ids")
    private boolean recipient_ids;
    @JsonProperty("text")
    private String text;
    @JsonProperty("cards_platform")
    private String cards_platform;
    @JsonProperty("include_cards")
    private int include_cards;
    @JsonProperty("include_quote_count")
    private boolean include_quote_count;
    @JsonProperty("dm_users")
    private boolean dm_users;
    @JsonProperty("media_id")
    private String mediaId;

    public MessageWrite(String conversation_id, String text) {
        this.conversation_id = conversation_id;
        this.recipient_ids = false;
        this.text = text;
        this.cards_platform = "Web-12";
        this.include_cards = 1;
        this.include_quote_count = true;
        this.dm_users = false;
    }

    public MessageWrite(String conversation_id, String text, String mediaId) {
        this.conversation_id = conversation_id;
        this.recipient_ids = false;
        this.text = text;
        this.cards_platform = "Web-12";
        this.include_cards = 1;
        this.include_quote_count = true;
        this.dm_users = false;
        this.mediaId = mediaId;
    }
}

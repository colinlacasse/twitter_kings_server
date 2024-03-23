package com.twittersfs.server.dtos.twitter.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlternateUserIdentifierInput {
    @JsonProperty("text")
    private String text;
    @JsonProperty("link")
    private String link;

    public AlternateUserIdentifierInput(String text) {
        this.text = text;
        this.link = "next_link";
    }
}

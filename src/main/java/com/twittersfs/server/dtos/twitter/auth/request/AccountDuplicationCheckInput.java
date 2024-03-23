package com.twittersfs.server.dtos.twitter.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountDuplicationCheckInput {
    @JsonProperty("link")
    private String link;

    public AccountDuplicationCheckInput() {
        this.link = "AccountDuplicationCheck_false";
    }
}

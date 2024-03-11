package com.twittersfs.server.services.twitter.auth.models.subtasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordInput {
    @JsonProperty("password")
    private String password;
    @JsonProperty("link")
    private String link;

    public PasswordInput(String password) {
        this.password = password;
        this.link = "next_link";
    }
}

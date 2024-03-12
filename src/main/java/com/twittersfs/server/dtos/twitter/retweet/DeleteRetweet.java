package com.twittersfs.server.dtos.twitter.retweet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteRetweet {
    @JsonProperty("variables")
    private DeleteVariables variables;

    public DeleteRetweet(String tweetId) {
        this.variables = new DeleteVariables(tweetId);
    }
}

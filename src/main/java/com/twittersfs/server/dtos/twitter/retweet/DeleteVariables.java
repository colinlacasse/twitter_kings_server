package com.twittersfs.server.dtos.twitter.retweet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeleteVariables {
    @JsonProperty("source_tweet_id")
    private String tweetId;
    @JsonProperty("dark_request")
    private boolean darkRequest;

    public DeleteVariables(String tweetId){
        this.tweetId = tweetId;
        this.darkRequest = false;
    }
}

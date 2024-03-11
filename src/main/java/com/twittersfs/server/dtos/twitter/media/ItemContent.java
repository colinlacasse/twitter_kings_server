package com.twittersfs.server.dtos.twitter.media;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemContent {
    @JsonProperty("itemType")
    private String itemType;

    @JsonProperty("__typename")
    private String typename;

    @JsonProperty("tweet_results")
    private TweetResults tweetResults;
}

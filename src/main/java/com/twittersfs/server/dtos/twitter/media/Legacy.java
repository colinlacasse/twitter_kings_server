package com.twittersfs.server.dtos.twitter.media;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Legacy {
    @JsonProperty("retweeted")
    private boolean retweeted;
    @JsonProperty("retweet_count")
    private Integer retweetCount;
    @JsonProperty("screen_name")
    private String screenName;
    @JsonProperty("followers_count")
    private Integer friendsCount;
    @JsonProperty("statuses_count")
    private Integer statusesCount;
    @JsonProperty("pinned_tweet_ids_str")
    private List<String> pinnedTweetIdsStr;
}

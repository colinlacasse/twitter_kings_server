package com.twittersfs.server.dtos.twitter.statistic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TimeSeriesDTO {
    @JsonProperty("orgImpressions")
    private List<Integer> orgImpressions;

    @JsonProperty("prImpressions")
    private List<Integer> prImpressions;

    @JsonProperty("impressions")
    private List<Integer> impressions;

    @JsonProperty("urlClicks")
    private List<Integer> urlClicks;

    @JsonProperty("retweets")
    private List<Integer> retweets;

    @JsonProperty("favorites")
    private List<Integer> favorites;

    @JsonProperty("replies")
    private List<Integer> replies;

    @JsonProperty("engagements")
    private List<Integer> engagements;

    @JsonProperty("engagementRate")
    private List<Double> engagementRate;
}

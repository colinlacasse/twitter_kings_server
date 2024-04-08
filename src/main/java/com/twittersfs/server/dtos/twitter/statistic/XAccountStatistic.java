package com.twittersfs.server.dtos.twitter.statistic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XAccountStatistic {
    @JsonProperty("startTime")
    private long startTime;
    @JsonProperty("endTime")
    private long endTime;
    @JsonProperty("totals")
    private TotalsDTO totals;
    @JsonProperty("timeSeries")
    private TimeSeriesDTO timeSeries;
}

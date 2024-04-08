package com.twittersfs.server.dtos.twitter.statistic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class XStatistic {
    @JsonProperty("xAccountStatistic")
    private XAccountStatistic xAccountStatistic;
    @JsonProperty("xAccountTimeZone")
    private XAccountTimeZone xAccountTimeZone;
}

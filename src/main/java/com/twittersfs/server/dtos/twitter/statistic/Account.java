package com.twittersfs.server.dtos.twitter.statistic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account {
    @JsonProperty("name")
    private String name;
    @JsonProperty("timezone")
    private String timezone;
}

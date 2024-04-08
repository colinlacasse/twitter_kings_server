package com.twittersfs.server.dtos.twitter.statistic;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XData {
    @JsonProperty("account")
    private Account account;
}

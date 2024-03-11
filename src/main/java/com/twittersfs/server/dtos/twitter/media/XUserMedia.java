package com.twittersfs.server.dtos.twitter.media;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XUserMedia {
    @JsonProperty("data")
    private Data data;
}

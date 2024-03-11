package com.twittersfs.server.dtos.twitter.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twittersfs.server.dtos.twitter.media.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XUserData {
    @JsonProperty("data")
    private Data data;
}

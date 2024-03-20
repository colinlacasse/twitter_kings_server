package com.twittersfs.server.dtos.twitter.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XGif {
    @JsonProperty("media_id")
    private long mediaId;

    @JsonProperty("media_id_string")
    private String mediaIdString;

    @JsonProperty("expires_after_secs")
    private int expiresAfterSecs;

    @JsonProperty("media_key")
    private String mediaKey;
}

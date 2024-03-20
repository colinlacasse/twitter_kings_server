package com.twittersfs.server.dtos.twitter.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XGifStatus {
    @JsonProperty("media_id")
    private long mediaId;

    @JsonProperty("media_id_string")
    private String mediaIdString;

    @JsonProperty("media_key")
    private String mediaKey;

    private int size;

    @JsonProperty("expires_after_secs")
    private int expiresAfterSecs;

    @JsonProperty("processing_info")
    private ProcessingInfo processingInfo;
}

package com.twittersfs.server.dtos.twitter.error;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class XApiError {
    @JsonProperty("errors")
    private List<XError> errors;
    @JsonProperty("data")
    private Object data;
}

package com.twittersfs.server.dtos.twitter.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class XLoginRequest {
    @JsonProperty("flow_token")
    private String flowToken;
    @JsonProperty("subtask_inputs")
    private List<SubtaskInput> subtaskInputs;
}

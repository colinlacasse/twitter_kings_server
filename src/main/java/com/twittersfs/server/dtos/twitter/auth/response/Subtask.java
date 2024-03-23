package com.twittersfs.server.dtos.twitter.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class Subtask {
    @JsonProperty("subtasks")
    private List<SubtaskId> subtasks;
    @JsonProperty("flow_token")
    private String flowToken;
}

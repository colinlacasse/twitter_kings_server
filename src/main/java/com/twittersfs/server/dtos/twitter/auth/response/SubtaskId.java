package com.twittersfs.server.dtos.twitter.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubtaskId {
    @JsonProperty("subtask_id")
    private String subtaskId;
}

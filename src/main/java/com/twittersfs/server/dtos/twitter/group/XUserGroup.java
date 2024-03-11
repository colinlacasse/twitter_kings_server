package com.twittersfs.server.dtos.twitter.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class XUserGroup {
    @JsonProperty("inbox_initial_state")
    private InboxInitialState inboxInitialState;
}

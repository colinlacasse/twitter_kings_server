package com.twittersfs.server.dtos.twitter.media;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Timeline {
    @JsonProperty("instructions")
    List<Instruction> instructions;
}

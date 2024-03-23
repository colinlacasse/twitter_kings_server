package com.twittersfs.server.dtos.twitter.auth.unclock;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class XCaptchaToken {
    private String authenticityToken;
    private String assignmentToken;
}

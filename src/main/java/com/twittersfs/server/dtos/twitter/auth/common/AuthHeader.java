package com.twittersfs.server.dtos.twitter.auth.common;

import com.twittersfs.server.dtos.twitter.auth.common.AuthCredential;
import lombok.Getter;
import lombok.Setter;
import okhttp3.Headers;

@Getter
@Setter
public class AuthHeader {
    private Headers headers;

    public AuthHeader(AuthCredential cred) {
        Headers.Builder headersBuilder = new Headers.Builder();

        if (cred.getAuthToken() != null) {
            headersBuilder.add("authorization", "Bearer " + cred.getAuthToken());
        }
        if (cred.getGuestToken() != null) {
            headersBuilder.add("x-guest-token", cred.getGuestToken());
        }
        if (cred.getCsrfToken() != null) {
            headersBuilder.add("x-csrf-token", cred.getCsrfToken());
        }
        if (cred.getCookies() != null) {
            headersBuilder.add("cookie", cred.getCookies());
        }

        this.headers = headersBuilder.build();
    }
}

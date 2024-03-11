package com.twittersfs.server.services.twitter.auth.models;

import com.twittersfs.server.services.twitter.auth.enums.EAuthenticationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthCredential {
    private String authToken;
    private String guestToken;
    private String csrfToken;
    private String cookies;
    private EAuthenticationType authenticationType;

    public AuthCredential() {
        this.authToken =
                "AAAAAAAAAAAAAAAAAAAAANRILgAAAAAAnNwIzUejRCOuH5E6I8xnZz4puTs%3D1Zv7ttfk8LF81IUq16cHjhLTvJu4FA33AGWWjCpTnA";
    }

    public AuthCredential(String[] cookies, String guestToken) {
        this.authToken =
                "AAAAAAAAAAAAAAAAAAAAANRILgAAAAAAnNwIzUejRCOuH5E6I8xnZz4puTs%3D1Zv7ttfk8LF81IUq16cHjhLTvJu4FA33AGWWjCpTnA";
        // If guest credentials given
        if (cookies == null && guestToken != null) {
            this.guestToken = guestToken;
            this.authenticationType = EAuthenticationType.GUEST;
        }
        // If login credentials given
        else if (cookies != null && guestToken != null) {
            AuthCookie parsedCookie = new AuthCookie(cookies);

            this.cookies = parsedCookie.toString();
            this.guestToken = guestToken;
            this.authenticationType = EAuthenticationType.LOGIN;
        }
        // If user credentials given
        else if (cookies != null) {
            AuthCookie parsedCookie = new AuthCookie(cookies);

            this.cookies = parsedCookie.toString();
            this.csrfToken = parsedCookie.getCt0();
            this.authenticationType = EAuthenticationType.USER;
        }
    }

    public AuthHeader toHeader() {
        return new AuthHeader(this);
    }
}

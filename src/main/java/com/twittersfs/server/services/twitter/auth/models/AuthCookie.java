package com.twittersfs.server.services.twitter.auth.models;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Getter
@Setter
@Slf4j
public class AuthCookie {
    private String kdt;
    private String twid;
    private String ct0;
    private String authToken;

    public AuthCookie(String[] cookieStr){
        String cookies = String.join(";", cookieStr);
        Pattern pattern = Pattern.compile("(\\w+)=(\"[^\"]+\"|[^;]+)");
        Matcher matcher = pattern.matcher(cookies);

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();

            switch (key) {
                case "kdt":
                    kdt = value;
                    break;
                case "twid":
                    twid = value;
                    break;
                case "ct0":
                    ct0 = value;
                    break;
                case "auth_token":
                    authToken = value;
                    break;
            }
        }
    }

    public String toString() {
        StringBuilder outStr = new StringBuilder();
        for (java.lang.reflect.Field field : this.getClass().getDeclaredFields()) {
            try {
                outStr.append(field.getName()).append("=").append(field.get(this)).append(";");
            } catch (IllegalAccessException e) {
                log.error("Parsing cookie error : " + e);
            }
        }
        return outStr.toString();
    }
}

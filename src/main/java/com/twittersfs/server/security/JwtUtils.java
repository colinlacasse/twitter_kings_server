package com.twittersfs.server.security;

import io.jsonwebtoken.Claims;

public class JwtUtils {
    public static JwtAuthentication generate(Claims claims) {
        final JwtAuthentication jwtInfoToken = new JwtAuthentication();
        jwtInfoToken.setRole(getRole(claims));
//        jwtInfoToken.setUsername(claims.get("username", String.class));
        jwtInfoToken.setEmail(claims.getSubject());
        return jwtInfoToken;
    }

    private static String getRole(Claims claims) {
        return claims.get("role", String.class);
    }
}

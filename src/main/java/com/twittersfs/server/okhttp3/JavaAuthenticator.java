package com.twittersfs.server.okhttp3;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class JavaAuthenticator extends Authenticator {
    private PasswordAuthentication auth;

    public JavaAuthenticator(String strUserName, String strPasswd) {
        auth = new PasswordAuthentication(strUserName, strPasswd.toCharArray());
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return auth;
    }
}

package com.twittersfs.server.okhttp3;

import com.twittersfs.server.entities.Proxy;
import lombok.AllArgsConstructor;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
@AllArgsConstructor
public class OkHttpAuthenticator implements Authenticator {
    private Proxy proxy;
    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NotNull Response response) throws IOException {
        String credential = Credentials.basic(proxy.getUsername(), proxy.getPassword());
        return response.request().newBuilder()
                .header("Proxy-Authorization", credential)
                .build();
    }
}

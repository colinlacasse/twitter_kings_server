package com.twittersfs.server.okhttp3;

import com.twittersfs.server.entities.Proxy;
import okhttp3.OkHttpClient;

public interface OkHttp3ClientService {
    OkHttpClient createClientWithProxy(Proxy proxy);
}

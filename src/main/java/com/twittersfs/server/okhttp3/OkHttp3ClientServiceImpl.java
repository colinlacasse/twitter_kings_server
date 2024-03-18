package com.twittersfs.server.okhttp3;

import com.twittersfs.server.entities.Proxy;
import com.twittersfs.server.enums.ProxyType;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;

@Service
public class OkHttp3ClientServiceImpl implements OkHttp3ClientService {
    @Override
    public OkHttpClient createClientWithProxy(Proxy proxy) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (proxy.getType() == ProxyType.HTTP) {
            configureHttpProxy(clientBuilder, proxy);
        } else {
            configureSocksProxy(clientBuilder, proxy);
        }
        return clientBuilder.build();
    }


    private void configureHttpProxy(OkHttpClient.Builder builder, Proxy proxy) {
        OkHttpAuthenticator proxyAuthenticator = new OkHttpAuthenticator(proxy);
        builder.proxy(new java.net.Proxy(java.net.Proxy.Type.HTTP,
                        new InetSocketAddress(proxy.getIp(), Integer.parseInt(proxy.getPort()))))
                .proxyAuthenticator(proxyAuthenticator);
    }

    private void configureSocksProxy(OkHttpClient.Builder builder, Proxy proxy) {
        InetSocketAddress proxyAddr = new InetSocketAddress(proxy.getIp(),
                Integer.parseInt(proxy.getPort()));
        java.net.Proxy proxy1 = new java.net.Proxy(java.net.Proxy.Type.SOCKS, proxyAddr);
        JavaAuthenticator proxyAuthenticator = new JavaAuthenticator(proxy.getUsername(), proxy.getPassword());
        java.net.Authenticator.setDefault(proxyAuthenticator);
        builder.proxy(proxy1);
    }
}

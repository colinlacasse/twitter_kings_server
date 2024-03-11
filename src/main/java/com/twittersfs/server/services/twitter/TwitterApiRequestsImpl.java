package com.twittersfs.server.services.twitter;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twittersfs.server.dtos.twitter.error.XApiError;
import com.twittersfs.server.dtos.twitter.media.XUserMedia;
import com.twittersfs.server.dtos.twitter.user.XUserData;
import com.twittersfs.server.entities.Proxy;
import com.twittersfs.server.exceptions.twitter.*;
import com.twittersfs.server.okhttp3.OkHttp3ClientService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class TwitterApiRequestsImpl implements TwitterApiRequests {
    private final OkHttp3ClientService okHttp3ClientService;

    public TwitterApiRequestsImpl(OkHttp3ClientService okHttp3ClientService) {
        this.okHttp3ClientService = okHttp3ClientService;
    }

    @Override
    public XUserData getUserByScreenName(String twitterAccountName, Proxy proxy, String cookies, String auth, String csrf) throws IOException {
        String url = "https://twitter.com/i/api/graphql/G3KGOASz96M-Qu0nwmGXNg/UserByScreenName?variables=%7B%22screen_name%22%3A%22" + twitterAccountName + "%22%2C%22withSafetyModeUserFields%22%3Atrue%7D&features=%7B%22hidden_profile_likes_enabled%22%3Atrue%2C%22hidden_profile_subscriptions_enabled%22%3Atrue%2C%22responsive_web_graphql_exclude_directive_enabled%22%3Atrue%2C%22verified_phone_label_enabled%22%3Afalse%2C%22subscriptions_verification_info_is_identity_verified_enabled%22%3Atrue%2C%22subscriptions_verification_info_verified_since_enabled%22%3Atrue%2C%22highlights_tweets_tab_ui_enabled%22%3Atrue%2C%22creator_subscriptions_tweet_preview_api_enabled%22%3Atrue%2C%22responsive_web_graphql_skip_user_profile_image_extensions_enabled%22%3Afalse%2C%22responsive_web_graphql_timeline_navigation_enabled%22%3Atrue%7D&fieldToggles=%7B%22withAuxiliaryUserLabels%22%3Afalse%7D";
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OkHttpClient client = okHttp3ClientService.createClientWithProxy(proxy);
        try (Response response = client.newCall(buildRequest(url,cookies,auth,csrf)).execute()) {
            String jsonResponse = response.body().string();
            if (!response.isSuccessful()) {
                handleErrorResponse(twitterAccountName, jsonResponse, mapper);
            }
            return mapper.readValue(jsonResponse, XUserData.class);
        }
    }

    @Override
    public XUserMedia getUserMedia(String userId, Proxy proxy, String cookies, String auth, String csrf) {
        return null;
    }

    private Request buildRequest(String url, String cookies, String auth, String csrf) {
        return new Request.Builder()
                .url(url)
                .addHeader("cookie", cookies)
                .addHeader("authority", "twitter.com")
                .addHeader("accept", "*/*")
                .addHeader("authorization", "Bearer " + auth)
                .addHeader("x-csrf-token", csrf)
                .build();
    }

    private void handleErrorResponse(String twitterAccountName, String jsonResponse, ObjectMapper mapper) throws IOException {
        XApiError err = mapper.readValue(jsonResponse, XApiError.class);
        if (err != null) {
            throwTwitterExceptions(twitterAccountName, err);
        } else {
            throw new IOException("Unexpected exception in account : " + twitterAccountName + " " + jsonResponse);
        }
    }

    private void throwTwitterExceptions(String twitterAccountName, XApiError err) {
        if (err.getErrors().get(0).getCode() == 32) {
            throw new XAccountAuthException(" " + err.getErrors().get(0).getMessage() + " " + twitterAccountName);
        } else if (err.getErrors().get(0).getCode() == 326) {
            throw new XAccountLockedException(" " + err.getErrors().get(0).getMessage() + " " + twitterAccountName);
        } else if (err.getErrors().get(0).getCode() == 141) {
            throw new XAccountSuspendedException(" " + err.getErrors().get(0).getMessage() + " " + twitterAccountName);
        } else if (err.getErrors().get(0).getCode() == 477) {
            throw new XAccountCooldownException(" " + err.getErrors().get(0).getMessage() + " " + twitterAccountName);
        } else if (err.getErrors().get(0).getCode() == 429) {
            throw new XAccountConnectException(" " + err.getErrors().get(0).getMessage() + " " + twitterAccountName);
        } else if (err.getErrors().get(0).getCode() == 402) {
            throw new XAccountProxyException(" " + err.getErrors().get(0).getMessage() + " " + twitterAccountName);
        } else if (err.getErrors().get(0).getCode() == 88) {
            throw new XAccountRateLimitException(" " + err.getErrors().get(0).getMessage() + " " + twitterAccountName);
        } else if (err.getErrors().get(0).getCode() == 61) {
            throw new XAccountPermissionException(" " + err.getErrors().get(0).getMessage() + " " + twitterAccountName);
        } else if (err.getErrors().get(0).getCode() == 130) {
            throw new XAccountOverCapacityException(" " + err.getErrors().get(0).getMessage() + " " + twitterAccountName);
        }
    }
}

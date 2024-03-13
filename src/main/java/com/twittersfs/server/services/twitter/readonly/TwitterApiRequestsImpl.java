package com.twittersfs.server.services.twitter.readonly;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twittersfs.server.dtos.twitter.error.XApiError;
import com.twittersfs.server.dtos.twitter.group.XUserGroup;
import com.twittersfs.server.dtos.twitter.media.XUserMedia;
import com.twittersfs.server.dtos.twitter.message.MessageWrite;
import com.twittersfs.server.dtos.twitter.message.XGroupMessage;
import com.twittersfs.server.dtos.twitter.retweet.DeleteRetweet;
import com.twittersfs.server.dtos.twitter.user.XUserData;
import com.twittersfs.server.entities.Proxy;
import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.exceptions.twitter.*;
import com.twittersfs.server.okhttp3.OkHttp3ClientService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.nonNull;

@Service
@Slf4j
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
        try (Response response = client.newCall(buildRequest(url, cookies, auth, csrf)).execute()) {
            String jsonResponse = response.body().string();
            if (!response.isSuccessful()) {
                handleErrorResponse(twitterAccountName, jsonResponse, mapper);
            }
            return mapper.readValue(jsonResponse, XUserData.class);
        }
    }

    @Override
    public XUserMedia getUserMedia(String twitterAccountName, String userId, Proxy proxy, String cookies, String auth, String csrf) throws IOException {
        String url = "https://twitter.com/i/api/graphql/dh2lDmjqEkxCWQK_UxkH4w/UserTweets?variables=%7B%22userId%22%3A%22" + userId + "%22%2C%22count%22%3A20%2C%22includePromotedContent%22%3Atrue%2C%22withQuickPromoteEligibilityTweetFields%22%3Atrue%2C%22withVoice%22%3Atrue%2C%22withV2Timeline%22%3Atrue%7D&features=%7B%22responsive_web_graphql_exclude_directive_enabled%22%3Atrue%2C%22verified_phone_label_enabled%22%3Afalse%2C%22responsive_web_home_pinned_timelines_enabled%22%3Atrue%2C%22creator_subscriptions_tweet_preview_api_enabled%22%3Atrue%2C%22responsive_web_graphql_timeline_navigation_enabled%22%3Atrue%2C%22responsive_web_graphql_skip_user_profile_image_extensions_enabled%22%3Afalse%2C%22c9s_tweet_anatomy_moderator_badge_enabled%22%3Atrue%2C%22tweetypie_unmention_optimization_enabled%22%3Atrue%2C%22responsive_web_edit_tweet_api_enabled%22%3Atrue%2C%22graphql_is_translatable_rweb_tweet_is_translatable_enabled%22%3Atrue%2C%22view_counts_everywhere_api_enabled%22%3Atrue%2C%22longform_notetweets_consumption_enabled%22%3Atrue%2C%22responsive_web_twitter_article_tweet_consumption_enabled%22%3Afalse%2C%22tweet_awards_web_tipping_enabled%22%3Afalse%2C%22freedom_of_speech_not_reach_fetch_enabled%22%3Atrue%2C%22standardized_nudges_misinfo%22%3Atrue%2C%22tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled%22%3Atrue%2C%22longform_notetweets_rich_text_read_enabled%22%3Atrue%2C%22longform_notetweets_inline_media_enabled%22%3Atrue%2C%22responsive_web_media_download_video_enabled%22%3Afalse%2C%22responsive_web_enhance_cards_enabled%22%3Afalse%7D";
        OkHttpClient client = okHttp3ClientService.createClientWithProxy(proxy);
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try (Response response = client.newCall(buildRequest(url, cookies, auth, csrf)).execute()) {
            String jsonResponse = response.body().string();
            if (!response.isSuccessful()) {
                handleErrorResponse(twitterAccountName, jsonResponse, mapper);
            }
            return mapper.readValue(jsonResponse, XUserMedia.class);
        }
    }

    @Override
    public XUserGroup getUserConversations(String twitterAccountName, String userId, Proxy proxy, String cookies, String auth, String csrf) throws IOException {
        OkHttpClient client = okHttp3ClientService.createClientWithProxy(proxy);
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String baseURL = "https://twitter.com/i/api/1.1/dm/user_updates.json";
        Map<String, String> params = getStringStringMap();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(baseURL).newBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
        }
        String url = urlBuilder.build().toString();
        try (Response response = client.newCall(buildRequest(url, cookies, auth, csrf)).execute()) {
            String jsonResponse = response.body().string();
            if (!response.isSuccessful()) {
                handleErrorResponse(twitterAccountName, jsonResponse, mapper);
            }
            return mapper.readValue(jsonResponse, XUserGroup.class);
        }
    }

    @Override
    public XGroupMessage getGroupMessages(String twitterAccountName, String groupId, Proxy proxy, String cookies, String auth, String csrf) throws IOException {
        OkHttpClient client = okHttp3ClientService.createClientWithProxy(proxy);
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String url = "https://twitter.com/i/api/1.1/dm/conversation/" + groupId + ".json?";
        try (Response response = client.newCall(buildRequest(url, cookies, auth, csrf)).execute()) {
            String jsonResponse = response.body().string();
            if (!response.isSuccessful()) {
                handleErrorResponse(twitterAccountName, jsonResponse, mapper);
            }
            return mapper.readValue(jsonResponse, XGroupMessage.class);
        }
    }

    @Override
    public void writeMessage(String twitterAccountName, String message, String groupId, Proxy proxy, String cookies, String auth, String csrf) throws IOException {
        String url = "https://twitter.com/i/api/1.1/dm/new2.json?ext=mediaColor%2CaltText%2CmediaStats%2ChighlightedLabel%2ChasNftAvatar%2CvoiceInfo%2CbirdwatchPivot%2CsuperFollowMetadata%2CunmentionInfo%2CeditControl&include_ext_alt_text=true&include_ext_limited_action_results=true&include_reply_count=1&tweet_mode=extended&include_ext_views=true&include_groups=true&include_inbox_timelines=true&include_ext_media_color=true&supports_reactions=true";
        OkHttpClient client = okHttp3ClientService.createClientWithProxy(proxy);
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MessageWrite payload = new MessageWrite(groupId, message);
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"), mapper.writeValueAsString(payload));
        buildRequestBody(twitterAccountName, cookies, auth, csrf, client, mapper, url, requestBody);
    }

    @Override
    public void retweet(String twitterAccountName, String postId, Proxy proxy, String cookies, String auth, String csrf) throws IOException {
        String url = "https://twitter.com/i/api/graphql/ojPdsZsimiJrUGLR1sjUtA/CreateRetweet";
        OkHttpClient client = okHttp3ClientService.createClientWithProxy(proxy);
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String payload = "{\"variables\":{\"tweet_id\":\"" + postId + "\",\"dark_request\":false},\"queryId\":\"ojPdsZsimiJrUGLR1sjUtA\"}";
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"), payload);
        try (Response response = client.newCall(buildRequest(requestBody, url, cookies, auth, csrf)).execute()) {
            String jsonResponse = response.body().string();
            if (!response.isSuccessful()) {
                handleErrorResponse(twitterAccountName, jsonResponse, mapper);
            }
        }
    }

    @Override
    public void deleteRetweet(String twitterAccountName, String postId, Proxy proxy, String cookies, String auth, String csrf) throws IOException {
        OkHttpClient client = okHttp3ClientService.createClientWithProxy(proxy);
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String url = "https://twitter.com/i/api/graphql/iQtK4dl5hBmXewYZuEOKVw/DeleteRetweet";
        DeleteRetweet payload = new DeleteRetweet(postId);
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"), mapper.writeValueAsString(payload));
        buildRequestBody(twitterAccountName, cookies, auth, csrf, client, mapper, url, requestBody);
    }

    @Override
    public void addGroupToAccount(TwitterAccount donor, String toUpdateRestId, String groupId) throws IOException {
        if (nonNull(toUpdateRestId)) {
            OkHttpClient client = okHttp3ClientService.createClientWithProxy(donor.getProxy());
            ObjectMapper mapper = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String json = "{\"variables\":{\"addedParticipants\":[\"" + toUpdateRestId + "\"],\"conversationId\":\"" + groupId + "\"},\"queryId\":\"oBwyQ0_xVbAQ8FAyG0pCRA\"}";
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody requestBody = RequestBody.create(mediaType, json);
            String url = "https://twitter.com/i/api/graphql/oBwyQ0_xVbAQ8FAyG0pCRA/AddParticipantsMutation";
            buildRequestBody(donor.getUsername(), donor.getCookie(), donor.getAuthToken(), donor.getCsrfToken(), client, mapper, url, requestBody);
        }
    }

    @Override
    public void subscribe(TwitterAccount twitterAccount, String subscribeOnRestId) throws IOException {
        OkHttpClient client = okHttp3ClientService.createClientWithProxy(twitterAccount.getProxy());
        String url = "https://twitter.com/i/api/1.1/friendships/create.json";
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        RequestBody requestBody = new FormBody.Builder()
                .add("include_profile_interstitial_type", "1")
                .add("include_blocking", "1")
                .add("include_blocked_by", "1")
                .add("include_followed_by", "1")
                .add("include_want_retweets", "1")
                .add("include_mute_edge", "1")
                .add("include_can_dm", "1")
                .add("include_can_media_tag", "1")
                .add("include_ext_has_nft_avatar", "1")
                .add("include_ext_is_blue_verified", "1")
                .add("include_ext_verified_type", "1")
                .add("include_ext_profile_image_shape", "1")
                .add("skip_status", "1")
                .add("user_id", subscribeOnRestId)
                .build();
        buildRequestBody(twitterAccount.getUsername(), twitterAccount.getCookie(), twitterAccount.getAuthToken(), twitterAccount.getCsrfToken(), client, mapper, url, requestBody);
    }

    @Override
    public void unsubscribe(TwitterAccount twitterAccount, String unsubscribeOnRestId) throws IOException {
        OkHttpClient client = okHttp3ClientService.createClientWithProxy(twitterAccount.getProxy());
        String url = "https://twitter.com/i/api/1.1/friendships/destroy.json";
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        RequestBody requestBody = new FormBody.Builder()
                .add("include_profile_interstitial_type", "1")
                .add("include_blocking", "1")
                .add("include_blocked_by", "1")
                .add("include_followed_by", "1")
                .add("include_want_retweets", "1")
                .add("include_mute_edge", "1")
                .add("include_can_dm", "1")
                .add("include_can_media_tag", "1")
                .add("include_ext_has_nft_avatar", "1")
                .add("include_ext_is_blue_verified", "1")
                .add("include_ext_verified_type", "1")
                .add("include_ext_profile_image_shape", "1")
                .add("skip_status", "1")
                .add("user_id", unsubscribeOnRestId)
                .build();
        buildRequestBody(twitterAccount.getUsername(), twitterAccount.getCookie(), twitterAccount.getAuthToken(), twitterAccount.getCsrfToken(), client, mapper, url, requestBody);
    }

    @Override
    public void setDmSettings(TwitterAccount twitterAccount) throws IOException {
        OkHttpClient client = okHttp3ClientService.createClientWithProxy(twitterAccount.getProxy());
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String url = "https://api.twitter.com/1.1/account/settings.json";
        RequestBody requestBody = new FormBody.Builder()
                .add("include_mention_filter", "true")
                .add("include_nsfw_user_flag", "true")
                .add("include_nsfw_admin_flag", "true")
                .add("include_ranked_timeline", "true")
                .add("include_alt_text_compose", "true")
                .add("allow_dms_from", "all")
                .add("dm_quality_filter", "disabled")
                .build();
        buildRequestBody(twitterAccount.getUsername(), twitterAccount.getCookie(), twitterAccount.getAuthToken(), twitterAccount.getCsrfToken(), client, mapper, url, requestBody);
    }

    private void buildRequestBody(String twitterAccountName, String cookies, String auth, String csrf, OkHttpClient client, ObjectMapper mapper, String url, RequestBody requestBody) throws IOException {
        try (Response response = client.newCall(buildRequest(requestBody, url, cookies, auth, csrf)).execute()) {
            String jsonResponse = response.body().string();
            if (!response.isSuccessful()) {
                handleErrorResponse(twitterAccountName, jsonResponse, mapper);
            }
        }
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

    private Request buildRequest(RequestBody body, String url, String cookies, String auth, String csrf) {
        return new Request.Builder()
                .url(url)
                .post(body)
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

    @NotNull
    private static Map<String, String> getStringStringMap() {
        Map<String, String> params = new HashMap<>();
        params.put("nsfw_filtering_enabled", "false");
        params.put("filter_low_quality", "true");
        params.put("include_quality", "all");
        params.put("dm_secret_conversations_enabled", "false");
        params.put("krs_registration_enabled", "true");
        params.put("cards_platform", "Web-12");
        params.put("include_cards", "1");
        params.put("include_ext_alt_text", "true");
        params.put("include_ext_limited_action_results", "true");
        params.put("include_quote_count", "true");
        params.put("include_reply_count", "1");
        params.put("tweet_mode", "extended");
        params.put("include_ext_views", "true");
        params.put("dm_users", "false");
        params.put("include_groups", "true");
        params.put("include_inbox_timelines", "true");
        params.put("include_ext_media_color", "true");
        params.put("supports_reactions", "true");
        params.put("include_ext_edit_control", "true");
        params.put("include_ext_business_affiliations_label", "true");
        params.put("ext", "mediaColor,altText,businessAffiliationsLabel,mediaStats,highlightedLabel,hasNftAvatar,voiceInfo,birdwatchPivot,superFollowMetadata,unmentionInfo,editControl");
        return params;
    }
}

package com.twittersfs.server.services.twitter.readonly;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twittersfs.server.dtos.twitter.auth.unclock.XAccessPageResp;
import com.twittersfs.server.dtos.twitter.auth.unclock.XCaptchaToken;
import com.twittersfs.server.dtos.twitter.error.XApiError;
import com.twittersfs.server.dtos.twitter.group.XUserGroup;
import com.twittersfs.server.dtos.twitter.media.XUserMedia;
import com.twittersfs.server.dtos.twitter.message.MessageWrite;
import com.twittersfs.server.dtos.twitter.message.XGif;
import com.twittersfs.server.dtos.twitter.message.XGifStatus;
import com.twittersfs.server.dtos.twitter.message.XGroupMessage;
import com.twittersfs.server.dtos.twitter.retweet.DeleteRetweet;
import com.twittersfs.server.dtos.twitter.statistic.XAccountStatistic;
import com.twittersfs.server.dtos.twitter.statistic.XAccountTimeZone;
import com.twittersfs.server.dtos.twitter.user.XUserData;
import com.twittersfs.server.entities.Proxy;
import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.exceptions.twitter.*;
import com.twittersfs.server.okhttp3.OkHttp3ClientService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

@Service
@Slf4j
public class TwitterApiRequestsImpl implements TwitterApiRequests {
    private final OkHttp3ClientService okHttp3ClientService;
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public TwitterApiRequestsImpl(OkHttp3ClientService okHttp3ClientService) {
        this.okHttp3ClientService = okHttp3ClientService;
    }

    @Override
    public XAccountStatistic getAccountStatistic(OkHttpClient client, TwitterAccount twitterAccount) throws IOException {
        String url = "https://analytics.twitter.com/user/" + twitterAccount.getUsername() + "/tweets/account_stats.json?";
        Request request = new Request.Builder().url(url)
                .addHeader("cookie", twitterAccount.getCookie())
                .addHeader("authority", "analytics.twitter.com")
                .addHeader("refer", "https://analytics.twitter.com/user/" + twitterAccount.getUsername() + "/tweets")
                .addHeader("accept", "*/*")
                .addHeader("x-csrf-token", twitterAccount.getCsrfToken())
                .build();
        try (Response response = client.newCall(request).execute()) {
            String jsonResponse = response.body().string();
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to get account statistic in account : " + twitterAccount.getUsername() + " : " + jsonResponse);
            }
            return mapper.readValue(jsonResponse, XAccountStatistic.class);
        }
    }

    @Override
    public XAccountTimeZone getAccountTimeZone(OkHttpClient client, TwitterAccount twitterAccount, String adsAccountId) throws IOException {
        String url = "https://ads-api.twitter.com/11/accounts/" + adsAccountId + "/perspective";
        Request request = new Request.Builder().url(url)
                .addHeader("cookie", twitterAccount.getCookie())
                .addHeader("authority", "ads-api.twitter.com")
                .addHeader("accept", "*/*")
                .addHeader("authorization", "Bearer AAAAAAAAAAAAAAAAAAAAAG5LOQEAAAAAbEKsIYYIhrfOQqm4H8u7xcahRkU%3Dz98HKmzbeXdKqBfUDmElcqYl0cmmKY9KdS2UoNIz3Phapgsowi")
                .addHeader("x-csrf-token", twitterAccount.getCsrfToken())
                .build();
        try (Response response = client.newCall(request).execute()) {
            String jsonResponse = response.body().string();
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to get account time zone : ");
            }
            return mapper.readValue(jsonResponse, XAccountTimeZone.class);
        }
    }
    @Override
    public String getAdsAccountId(OkHttpClient client, TwitterAccount twitterAccount) throws IOException {
        String url = "https://analytics.twitter.com/user/" + twitterAccount.getUsername() + "/home";
        Request request = new Request.Builder().url(url)
                .addHeader("cookie", twitterAccount.getCookie())
                .addHeader("authority", "analytics.twitter.com")
//                .addHeader("refer", "https://analytics.twitter.com/user/" + twitterAccount.getUsername() + "/tweets")
                .addHeader("accept", "*/*")
                .addHeader("x-csrf-token", twitterAccount.getCsrfToken())
                .build();
        try (Response response = client.newCall(request).execute()) {
            String jsonResponse = response.body().string();
            Document doc = Jsoup.parse(jsonResponse);
            Element scriptTag = doc.selectFirst("#webaf-navbar-data");
            String jsonData = scriptTag.data();
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to get ads account id : " + twitterAccount.getUsername() + " : " + jsonResponse);
            }
            JsonNode jsonNode = mapper.readTree(jsonData);
            return jsonNode.get("adsAccountId").asText();
        }
    }

    @Override
    public XUserData getUserByScreenName(OkHttpClient client, String twitterAccountName, Proxy proxy, String cookies, String auth, String csrf) throws IOException {
        String url = "https://twitter.com/i/api/graphql/G3KGOASz96M-Qu0nwmGXNg/UserByScreenName?variables=%7B%22screen_name%22%3A%22" + twitterAccountName + "%22%2C%22withSafetyModeUserFields%22%3Atrue%7D&features=%7B%22hidden_profile_likes_enabled%22%3Atrue%2C%22hidden_profile_subscriptions_enabled%22%3Atrue%2C%22responsive_web_graphql_exclude_directive_enabled%22%3Atrue%2C%22verified_phone_label_enabled%22%3Afalse%2C%22subscriptions_verification_info_is_identity_verified_enabled%22%3Atrue%2C%22subscriptions_verification_info_verified_since_enabled%22%3Atrue%2C%22highlights_tweets_tab_ui_enabled%22%3Atrue%2C%22creator_subscriptions_tweet_preview_api_enabled%22%3Atrue%2C%22responsive_web_graphql_skip_user_profile_image_extensions_enabled%22%3Afalse%2C%22responsive_web_graphql_timeline_navigation_enabled%22%3Atrue%7D&fieldToggles=%7B%22withAuxiliaryUserLabels%22%3Afalse%7D";
//        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        OkHttpClient client = okHttp3ClientService.createClientWithProxy(proxy);
        try (Response response = client.newCall(buildRequest(url, cookies, auth, csrf)).execute()) {
            String jsonResponse = response.body().string();
            if (!response.isSuccessful()) {
                handleErrorResponse(twitterAccountName, jsonResponse, mapper);
            }
            return mapper.readValue(jsonResponse, XUserData.class);
        }
    }

    @Override
    public XUserMedia getUserMedia(OkHttpClient client, String twitterAccountName, String userId, Proxy proxy, String cookies, String auth, String csrf) throws IOException {
        String url = "https://twitter.com/i/api/graphql/dh2lDmjqEkxCWQK_UxkH4w/UserTweets?variables=%7B%22userId%22%3A%22" + userId + "%22%2C%22count%22%3A20%2C%22includePromotedContent%22%3Atrue%2C%22withQuickPromoteEligibilityTweetFields%22%3Atrue%2C%22withVoice%22%3Atrue%2C%22withV2Timeline%22%3Atrue%7D&features=%7B%22responsive_web_graphql_exclude_directive_enabled%22%3Atrue%2C%22verified_phone_label_enabled%22%3Afalse%2C%22responsive_web_home_pinned_timelines_enabled%22%3Atrue%2C%22creator_subscriptions_tweet_preview_api_enabled%22%3Atrue%2C%22responsive_web_graphql_timeline_navigation_enabled%22%3Atrue%2C%22responsive_web_graphql_skip_user_profile_image_extensions_enabled%22%3Afalse%2C%22c9s_tweet_anatomy_moderator_badge_enabled%22%3Atrue%2C%22tweetypie_unmention_optimization_enabled%22%3Atrue%2C%22responsive_web_edit_tweet_api_enabled%22%3Atrue%2C%22graphql_is_translatable_rweb_tweet_is_translatable_enabled%22%3Atrue%2C%22view_counts_everywhere_api_enabled%22%3Atrue%2C%22longform_notetweets_consumption_enabled%22%3Atrue%2C%22responsive_web_twitter_article_tweet_consumption_enabled%22%3Afalse%2C%22tweet_awards_web_tipping_enabled%22%3Afalse%2C%22freedom_of_speech_not_reach_fetch_enabled%22%3Atrue%2C%22standardized_nudges_misinfo%22%3Atrue%2C%22tweet_with_visibility_results_prefer_gql_limited_actions_policy_enabled%22%3Atrue%2C%22longform_notetweets_rich_text_read_enabled%22%3Atrue%2C%22longform_notetweets_inline_media_enabled%22%3Atrue%2C%22responsive_web_media_download_video_enabled%22%3Afalse%2C%22responsive_web_enhance_cards_enabled%22%3Afalse%7D";
//        OkHttpClient client = okHttp3ClientService.createClientWithProxy(proxy);
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        try (Response response = client.newCall(buildRequest(url, cookies, auth, csrf)).execute()) {
            String jsonResponse = response.body().string();
            if (!response.isSuccessful()) {
                handleErrorResponse(twitterAccountName, jsonResponse, mapper);
            }
            return mapper.readValue(jsonResponse, XUserMedia.class);
        }
    }

    @Override
    public XUserGroup getUserConversations(OkHttpClient client, String twitterAccountName, String userId, Proxy proxy, String cookies, String auth, String csrf) throws IOException {
//        OkHttpClient client = okHttp3ClientService.createClientWithProxy(proxy);
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
    public XGroupMessage getGroupMessages(OkHttpClient client, String twitterAccountName, String groupId, Proxy proxy, String cookies, String auth, String csrf) throws IOException {
//        OkHttpClient client = okHttp3ClientService.createClientWithProxy(proxy);
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
    public void writeMessage(OkHttpClient client, String twitterAccountName, String message, String groupId, Proxy proxy, String cookies, String auth, String csrf, String mediaId) throws IOException {
//        OkHttpClient client = okHttp3ClientService.createClientWithProxy(proxy);
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .setSerializationInclusion(JsonInclude.Include.NON_NULL);

        if (nonNull(mediaId)) {
            String url = "https://twitter.com/i/api/1.1/dm/new2.json?ext=mediaColor%2CaltText%2CmediaStats%2ChighlightedLabel%2CvoiceInfo%2CbirdwatchPivot%2CsuperFollowMetadata%2CunmentionInfo%2CeditControl&include_ext_alt_text=true&include_ext_limited_action_results=true&include_reply_count=1&tweet_mode=extended&include_ext_views=true&include_groups=true&include_inbox_timelines=true&include_ext_media_color=true&supports_reactions=true";
            MessageWrite payload = new MessageWrite(groupId, message, mediaId);
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"), mapper.writeValueAsString(payload));
            buildRequestBody(twitterAccountName, cookies, auth, csrf, client, mapper, url, requestBody);
        } else {
            String url = "https://twitter.com/i/api/1.1/dm/new2.json?ext=mediaColor%2CaltText%2CmediaStats%2ChighlightedLabel%2ChasNftAvatar%2CvoiceInfo%2CbirdwatchPivot%2CsuperFollowMetadata%2CunmentionInfo%2CeditControl&include_ext_alt_text=true&include_ext_limited_action_results=true&include_reply_count=1&tweet_mode=extended&include_ext_views=true&include_groups=true&include_inbox_timelines=true&include_ext_media_color=true&supports_reactions=true";
            MessageWrite payload = new MessageWrite(groupId, message);
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json"), mapper.writeValueAsString(payload));
            buildRequestBody(twitterAccountName, cookies, auth, csrf, client, mapper, url, requestBody);
        }
    }

    @Override
    public void retweet(OkHttpClient client, String twitterAccountName, String postId, Proxy proxy, String cookies, String auth, String csrf) throws IOException {
        String url = "https://twitter.com/i/api/graphql/ojPdsZsimiJrUGLR1sjUtA/CreateRetweet";
//        OkHttpClient client = okHttp3ClientService.createClientWithProxy(proxy);
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
    public void deleteRetweet(OkHttpClient client, String twitterAccountName, String postId, Proxy proxy, String cookies, String auth, String csrf) throws IOException {
//        OkHttpClient client = okHttp3ClientService.createClientWithProxy(proxy);
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
//            ObjectMapper mapper = new ObjectMapper()
//                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                    .setSerializationInclusion(JsonInclude.Include.NON_NULL);
            String json = "{\"variables\":{\"addedParticipants\":[\"" + toUpdateRestId + "\"],\"conversationId\":\"" + groupId + "\"},\"queryId\":\"oBwyQ0_xVbAQ8FAyG0pCRA\"}";
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody requestBody = RequestBody.create(mediaType, json);
            String url = "https://twitter.com/i/api/graphql/oBwyQ0_xVbAQ8FAyG0pCRA/AddParticipantsMutation";
            buildRequestBody(donor.getUsername(), donor.getCookie(), donor.getAuthToken(), donor.getCsrfToken(), client, mapper, url, requestBody);
        }
    }

    @Override
    public XGif getGifMediaId(OkHttpClient client, TwitterAccount twitterAccount, String gifUrl) throws IOException {
//        OkHttpClient client = okHttp3ClientService.createClientWithProxy(twitterAccount.getProxy());
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String url = "https://upload.twitter.com/i/media/upload.json?command=INIT&source_url=" + gifUrl + "&media_type=image%2Fgif&media_category=dm_gif";
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.get("application/json"), ""))
                .header("authority", "upload.twitter.com")
                .header("accept", "*/*")
                .header("authorization", twitterAccount.getAuthToken())
                .header("cookie", twitterAccount.getCookie())
                .header("origin", "https://twitter.com")
                .header("referer", "https://twitter.com/")
                .header("x-csrf-token", twitterAccount.getCsrfToken())
                .build();
        try (Response response = client.newCall(request).execute()) {
            String jsonResponse = response.body().string();
            if (!response.isSuccessful()) {
                handleErrorResponse(twitterAccount.getUsername(), jsonResponse, mapper);
            }
            return mapper.readValue(jsonResponse, XGif.class);
        }
    }

    @Override
    public XGifStatus checkGifStatus(OkHttpClient client, TwitterAccount twitterAccount, String mediaId) throws IOException {
//        OkHttpClient client = okHttp3ClientService.createClientWithProxy(twitterAccount.getProxy());
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String url = "https://upload.twitter.com/i/media/upload.json?command=STATUS&media_id=" + mediaId;
        try (Response response = client.newCall(buildRequest(url, twitterAccount.getCookie(), twitterAccount.getAuthToken(), twitterAccount.getCsrfToken())).execute()) {
            String jsonResponse = response.body().string();
            if (!response.isSuccessful()) {
                handleErrorResponse(twitterAccount.getUsername(), jsonResponse, mapper);
            }
            return mapper.readValue(jsonResponse, XGifStatus.class);
        }
    }

    @Override
    public void subscribe(TwitterAccount twitterAccount, String subscribeOnRestId) throws IOException {
        OkHttpClient client = okHttp3ClientService.createClientWithProxy(twitterAccount.getProxy());
        String url = "https://twitter.com/i/api/1.1/friendships/create.json";
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
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
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
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

//    public String getGuestCreds(TwitterAccount twitterAccount) throws IOException {
//        OkHttpClient client = okHttp3ClientService.createClientWithProxy(twitterAccount.getProxy());
//        String url = "https://api.twitter.com/1.1/guest/activate.json";
//        Request request = new Request.Builder()
//                .url(url)
//                .post(RequestBody.create(null, new byte[0]))
//                .addHeader("cookie", twitterAccount.getCookie())
//                .addHeader("authority", "twitter.com")
//                .addHeader("accept", "*/*")
//                .addHeader("authorization", "Bearer " + twitterAccount.getAuthToken())
//                .addHeader("x-csrf-token", twitterAccount.getCsrfToken())
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            String jsonResponse = response.body().string();
//            if (!response.isSuccessful()) {
//                throw new IOException("Get guest creds failed : " + response);
//            }
//            JsonNode jsonNode = mapper.readTree(jsonResponse);
////            log.info("GUEST TOKEN : " + jsonNode.get("guest_token").asText());
//            return jsonNode.get("guest_token").asText();
//        }
//    }
//
//    public InitLogin initiateLogin(TwitterAccount twitterAccount, String guestToken) throws IOException {
//        OkHttpClient client = okHttp3ClientService.createClientWithProxy(twitterAccount.getProxy());
//        String url = "https://api.twitter.com/1.1/onboarding/task.json?flow_name=login";
//        Request request = new Request.Builder()
//                .url(url)
//                .post(RequestBody.create(null, new byte[0]))
//                .addHeader("x-guest-token", guestToken)
//                .addHeader("authority", "twitter.com")
//                .addHeader("accept", "*/*")
//                .addHeader("authorization", "Bearer " + twitterAccount.getAuthToken())
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            String[] setCookieHeaders = response.headers("Set-Cookie").toArray(new String[0]);
//            String cookies = String.join(";", setCookieHeaders);
//            String jsonResponse = response.body().string();
//            if (!response.isSuccessful()) {
//                throw new IOException("Initiate login failed: " + response);
//            }
//            String flowToken = mapper.readValue(jsonResponse, FlowToken.class).getFlowToken();
//            InitLogin initLogin = new InitLogin();
//            initLogin.setFlowToken(flowToken);
//            initLogin.setCookies(cookies);
////            log.info("FLOW TOKEN : " + flowToken);
//            return initLogin;
//        }
//    }
//
//    public Response postLoginData(TwitterAccount twitterAccount, String guestToken, LoginSubtaskPayload payload, String cookies) throws IOException {
//        OkHttpClient client = okHttp3ClientService.createClientWithProxy(twitterAccount.getProxy());
//        String url = "https://api.twitter.com/1.1/onboarding/task.json";
//        RequestBody requestBody = RequestBody.create(
//                MediaType.parse("application/json"), mapper.writeValueAsString(payload));
//
//        Request request = new Request.Builder()
//                .url(url)
//                .post(requestBody)
//                .addHeader("x-guest-token", guestToken)
//                .addHeader("authority", "twitter.com")
//                .addHeader("accept", "*/*")
//                .addHeader("authorization", "Bearer " + twitterAccount.getAuthToken())
//                .addHeader("cookies", cookies)
//                .build();
//
//        try (Response response = client.newCall(request).execute()) {
////            String jsonResponse = response.body().string();
//            if (!response.isSuccessful()) {
//                throw new IOException("Post login data failed: " + response.body().string());
//            }
//            return response;
////            String flowToken = mapper.readValue(jsonResponse, FlowToken.class).getFlowToken();
////            log.info("FLOW TOKEN : " + flowToken);
//        }
//    }

    public XAccessPageResp getAccessPage(TwitterAccount twitterAccount) throws IOException {
        OkHttpClient client = okHttp3ClientService.createClientWithProxy(twitterAccount.getProxy());
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String url = "https://twitter.com/account/access";
//        log.info("CSRF : " + twitterAccount.getCsrfToken());
        try (Response response = client.newCall(buildRequestWithUserAgent(url, twitterAccount.getCookie(), twitterAccount.getAuthToken(), twitterAccount.getCsrfToken())).execute()) {
            String jsonResponse = response.body().string();
            String[] setCookieHeaders = response.headers("Set-Cookie").toArray(new String[0]);
            String cookies = String.join(";", setCookieHeaders);
//            log.info("COOKIES : " + cookies);
//            log.info("GET ACCESS RESP : " + jsonResponse);
            if (!response.isSuccessful()) {
                handleErrorResponse(twitterAccount.getUsername(), jsonResponse, mapper);
            }
            return XAccessPageResp.builder()
                    .cookies(cookies)
                    .html(jsonResponse)
                    .build();
        }
    }

    public String postToAccessPage(TwitterAccount twitterAccount, XCaptchaToken tokens, String jsInst, String cookies) throws IOException {
        OkHttpClient client = okHttp3ClientService.createClientWithProxy(twitterAccount.getProxy());
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        log.info("Token AU : " + tokens.getAuthenticityToken());
//        log.info("Token AS : " + tokens.getAssignmentToken());
        RequestBody formBody = new FormBody.Builder()
                .add("authenticity_token", tokens.getAuthenticityToken())
                .add("assignment_token", tokens.getAssignmentToken())
                .add("lang", "en")
                .add("flow", "")
                .add("ui_metrics", jsInst)
                .build();

        return buildAccessPageRequest(client, formBody, twitterAccount, cookies);
    }

    public String postToAccessPageWithToken(TwitterAccount twitterAccount, XCaptchaToken tokens, String capsolverToken, String cookies) throws IOException {
        OkHttpClient client = okHttp3ClientService.createClientWithProxy(twitterAccount.getProxy());
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        log.info("Token AU : " + tokens.getAuthenticityToken());
//        log.info("Token AS : " + tokens.getAssignmentToken());
        RequestBody formBody = new FormBody.Builder()
                .add("authenticity_token", tokens.getAuthenticityToken())
                .add("assignment_token", tokens.getAssignmentToken())
                .add("lang", "en")
                .add("flow", "")
                .add("verification_string", capsolverToken)
                .add("language_code", "en")
                .build();

        return buildAccessPageRequest(client, formBody, twitterAccount, cookies);
    }

    @NotNull
    private String buildAccessPageRequest(OkHttpClient client, RequestBody formBody, TwitterAccount twitterAccount, String cookies) throws IOException {
//        log.info("COOKES : " + cookies);
        Request request = new Request.Builder()
                .url("https://twitter.com/account/access")
                .addHeader("Connection", "keep-alive")
                .addHeader("Host", "twitter.com")
                .addHeader("cookie", cookies + "; " + twitterAccount.getCookie())
                .addHeader("cache-control", "max-age=0")
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .addHeader("origin", "https://twitter.com")
                .addHeader("referer", "https://twitter.com/account/access")
                .addHeader("authority", "twitter.com")
                .addHeader("accept", "*/*")
                .addHeader("x-csrf-token", twitterAccount.getCsrfToken())
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String resp = response.body().string();
//            log.info("POST TO ACCESS RESP  : " + resp);
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            return resp;
        }
    }

    public String getJsInst(TwitterAccount twitterAccount) throws IOException {
        OkHttpClient client = okHttp3ClientService.createClientWithProxy(twitterAccount.getProxy());
        Request request = new Request.Builder()
                .url("https://twitter.com/i/js_inst?c_name=ui_metrics")
                .build();
        try (Response response = client.newCall(request).execute()) {
            String resp = response.body().string();
//            log.info("JS INST RESP : " + resp);
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

//            Pattern pattern = Pattern.compile("return\\s*\\{.*?\\};", Pattern.DOTALL);
            Pattern pattern = Pattern.compile("return\\s*\\{(.*?)};", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(resp);

            if (matcher.find()) {
//                log.info("MATCHER : " + matcher.group(1));
                return matcher.group(1);
            } else {
                throw new IOException("No match found for pattern in JS script");
            }
//            return Jsoup.parse(resp).text();
        }
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

    private Request buildRequestWithUserAgent(String url, String cookies, String auth, String csrf) {
        return new Request.Builder()
                .url(url)
                .get()
                .addHeader("cookie", cookies)
                .addHeader("authority", "twitter.com")
                .addHeader("accept", "*/*")
                .addHeader("upgrade-insecure-requests", "1")
                .addHeader("x-csrf-token", csrf)
                .build();
    }

    private void handleErrorResponse(String twitterAccountName, String jsonResponse, ObjectMapper mapper) throws IOException {
        XApiError err = mapper.readValue(jsonResponse, XApiError.class);
        if (err != null) {
            throwTwitterExceptions(twitterAccountName, err);
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
        } else if (err.getErrors().get(0).getCode() == 403) {
            throw new XAccountAuthException(" " + err.getErrors().get(0).getMessage() + " " + twitterAccountName);
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

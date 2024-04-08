package com.twittersfs.server.services.twitter.readonly;

import com.twittersfs.server.dtos.twitter.auth.response.InitLogin;
import com.twittersfs.server.dtos.twitter.auth.unclock.XAccessPageResp;
import com.twittersfs.server.dtos.twitter.group.XUserGroup;
import com.twittersfs.server.dtos.twitter.media.XUserMedia;
import com.twittersfs.server.dtos.twitter.message.XGif;
import com.twittersfs.server.dtos.twitter.message.XGifStatus;
import com.twittersfs.server.dtos.twitter.message.XGroupMessage;
import com.twittersfs.server.dtos.twitter.statistic.XAccountStatistic;
import com.twittersfs.server.dtos.twitter.statistic.XAccountTimeZone;
import com.twittersfs.server.dtos.twitter.user.XUserData;
import com.twittersfs.server.entities.Proxy;
import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.dtos.twitter.auth.unclock.XCaptchaToken;
import com.twittersfs.server.dtos.twitter.auth.request.LoginSubtaskPayload;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.io.IOException;

public interface TwitterApiRequests {
    XUserData getUserByScreenName(OkHttpClient client,String twitterAccountName, Proxy proxy, String cookies, String auth, String csrf) throws IOException;

    XUserMedia getUserMedia(OkHttpClient client, String twitterAccountName, String userId, Proxy proxy, String cookies, String auth, String csrf) throws IOException;
    XUserGroup getUserConversations(OkHttpClient client,String twitterAccountName, String userId, Proxy proxy, String cookies, String auth, String csrf) throws IOException;
    XGroupMessage getGroupMessages(OkHttpClient client,String twitterAccountName, String groupId, Proxy proxy, String cookies, String auth, String csrf) throws IOException;
    void writeMessage(OkHttpClient client,String twitterAccountName, String message, String groupId, Proxy proxy, String cookies, String auth, String csrf, String mediaId) throws IOException;
    void retweet(OkHttpClient client,String twitterAccountName, String postId, Proxy proxy, String cookies, String auth, String csrf) throws IOException;
    void deleteRetweet(OkHttpClient client,String twitterAccountName, String postId, Proxy proxy, String cookies, String auth, String csrf) throws IOException;
    void addGroupToAccount(TwitterAccount donor, String toUpdateRestId, String groupId) throws IOException;
    void subscribe(TwitterAccount twitterAccount, String restId) throws IOException;
    void unsubscribe(TwitterAccount twitterAccount, String unsubscribeOnRestId) throws IOException;
    void setDmSettings(TwitterAccount twitterAccount) throws IOException;
    XGif getGifMediaId(OkHttpClient client,TwitterAccount twitterAccount, String gifUrl) throws IOException;
    XGifStatus checkGifStatus(OkHttpClient client,TwitterAccount twitterAccount, String mediaId) throws IOException;
    XAccessPageResp getAccessPage(TwitterAccount twitterAccount) throws IOException;
    String postToAccessPageWithToken(TwitterAccount twitterAccount, XCaptchaToken tokens, String capsolverToken, String cookies) throws IOException;
    String postToAccessPage(TwitterAccount twitterAccount, XCaptchaToken tokens, String jsInst, String cookies) throws IOException;
    String getJsInst(TwitterAccount twitterAccount) throws IOException;
    XAccountStatistic getAccountStatistic(OkHttpClient client, TwitterAccount twitterAccount) throws IOException;
    XAccountTimeZone getAccountTimeZone(OkHttpClient client, TwitterAccount twitterAccount, String adsAccountId) throws IOException;
    String getAdsAccountId(OkHttpClient client, TwitterAccount twitterAccount) throws IOException;
//    String getGuestCreds(TwitterAccount twitterAccount) throws IOException;
//    InitLogin initiateLogin(TwitterAccount twitterAccount, String guestId) throws IOException;
//    Response postLoginData(TwitterAccount twitterAccount, String guestToken, LoginSubtaskPayload payload, String cookies) throws IOException;
}

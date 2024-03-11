package com.twittersfs.server.services.twitter.readonly;

import com.twittersfs.server.dtos.twitter.group.XUserGroup;
import com.twittersfs.server.dtos.twitter.media.XUserMedia;
import com.twittersfs.server.dtos.twitter.message.XGroupMessage;
import com.twittersfs.server.dtos.twitter.user.XUserData;
import com.twittersfs.server.entities.Proxy;

import java.io.IOException;

public interface TwitterApiRequests {
    XUserData getUserByScreenName(String twitterAccountName, Proxy proxy, String cookies, String auth, String csrf) throws IOException;

    XUserMedia getUserMedia(String twitterAccountName, String userId, Proxy proxy, String cookies, String auth, String csrf) throws IOException;
    XUserGroup getUserConversations(String twitterAccountName, String userId, Proxy proxy, String cookies, String auth, String csrf) throws IOException;
    XGroupMessage getGroupMessages(String twitterAccountName, String groupId, Proxy proxy, String cookies, String auth, String csrf) throws IOException;
    void writeMessage(String twitterAccountName,String message, String groupId, Proxy proxy, String cookies, String auth, String csrf) throws IOException;
    void retweet(String twitterAccountName, String postId, Proxy proxy, String cookies, String auth, String csrf) throws IOException;
    void deleteRetweet(String twitterAccountName, String postId, Proxy proxy, String cookies, String auth, String csrf) throws IOException;
}
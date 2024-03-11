package com.twittersfs.server.services.twitter;

import com.twittersfs.server.dtos.twitter.media.XUserMedia;
import com.twittersfs.server.dtos.twitter.user.XUserData;
import com.twittersfs.server.entities.Proxy;

import java.io.IOException;

public interface TwitterApiRequests {
    XUserData getUserByScreenName(String twitterAccountName, Proxy proxy, String cookies, String auth, String csrf) throws IOException;
    XUserMedia getUserMedia(String userId, Proxy proxy, String cookies, String auth, String csrf);
}

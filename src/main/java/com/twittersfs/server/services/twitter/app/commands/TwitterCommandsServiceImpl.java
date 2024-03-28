package com.twittersfs.server.services.twitter.app.commands;

import com.twittersfs.server.dtos.twitter.group.Conversation;
import com.twittersfs.server.dtos.twitter.group.XUserGroup;
import com.twittersfs.server.dtos.twitter.media.*;
import com.twittersfs.server.dtos.twitter.message.*;
import com.twittersfs.server.dtos.twitter.user.XUserData;
import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.entities.TwitterChatMessage;
import com.twittersfs.server.enums.TwitterAccountStatus;
import com.twittersfs.server.exceptions.twitter.*;
import com.twittersfs.server.services.TwitterAccountService;
import com.twittersfs.server.services.twitter.readonly.TwitterApiRequests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

@Service
@Slf4j
public class TwitterCommandsServiceImpl implements TwitterCommandsService {
    private final TwitterApiRequests twitterApiRequests;
    private final TwitterAccountService twitterAccountService;
    Set<Long> workingAccounts = ConcurrentHashMap.newKeySet();

    public TwitterCommandsServiceImpl(TwitterApiRequests twitterApiRequests, TwitterAccountService twitterAccountService) {
        this.twitterApiRequests = twitterApiRequests;
        this.twitterAccountService = twitterAccountService;
    }

    @Override
    public void checkIfAccountRunning(Long accountId) {
        if (!workingAccounts.isEmpty()) {
            for (Long id : workingAccounts) {
                if (Objects.equals(id, accountId)) {
                    throw new RuntimeException("Account is already working");
                }
            }
        }
    }

    @Override
    public void stop(Long twitterAccountId) {
        TwitterAccount account = twitterAccountService.get(twitterAccountId);

        if (!workingAccounts.isEmpty()) {
            for (Long id : workingAccounts) {
                if (Objects.equals(id, twitterAccountId)) {
                    twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.STOPPING);
                } else if (account.getStatus().equals(TwitterAccountStatus.STOPPING)) {
                    workingAccounts.remove(twitterAccountId);
                    twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.DISABLED);
                }
            }
        } else {
            twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.DISABLED);
        }
    }

    @Override
    public void execute(Long twitterAccountId) {
        workingAccounts.add(twitterAccountId);
        twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.ACTIVE);
        TwitterAccount twitterAccount = twitterAccountService.get(twitterAccountId);
        if (!nonNull(twitterAccount.getCsrfToken())) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.INVALID_COOKIES);
            workingAccounts.remove(twitterAccount.getId());
            return;
        }

        XUserData userData = getUserByScreenName(twitterAccount);
        if (!nonNull(twitterAccount.getRestId())) {
            if (nonNull(userData)) {
                twitterAccountService.updateRestId(twitterAccountId, userData.getData().getUser().getResult().getRestId());
            }
        }

        TwitterAccountStatus status = twitterAccount.getStatus();
        int groupTryCounter = 0;
        while (status.equals(TwitterAccountStatus.ACTIVE) || status.equals(TwitterAccountStatus.COOLDOWN) || status.equals(TwitterAccountStatus.UPDATED_COOKIES)) {
            if (isNotExpired(twitterAccount)) {
                try {
                    if (!nonNull(twitterAccount.getCsrfToken())) {
                        twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.INVALID_COOKIES);
                        workingAccounts.remove(twitterAccount.getId());
                        return;
                    }
                    if (status.equals(TwitterAccountStatus.COOLDOWN)) {
                        twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.ACTIVE);
                    }
                    twitterAccount = twitterAccountService.get(twitterAccountId);
                    XUserGroup groups = getUserConversations(twitterAccount);
                    int friendsBefore = twitterAccount.getFriends();
                    int messagesBefore = twitterAccount.getMessagesSent();
                    int retweetsBefore = twitterAccount.getRetweets();
                    if (nonNull(groups)) {
                        int retweetCounter = 0;
                        groupTryCounter = 0;
                        updateGroupsValue(groups, twitterAccountId);
                        for (Conversation conversation : groups.getInboxInitialState().getConversations().values()) {
                            if (nonNull(conversation.getName())) {
                                try {
                                    retweetCounter = processGroup(twitterAccount, conversation.getConversationID(), groupPostToRetweetParser(conversation.getName()), retweetCounter);
                                    if (retweetCounter >= 48) {
                                        break;
                                    }
                                } catch (InterruptedException e) {
                                    log.error("Interrupted Exception in Executed method , account : " + twitterAccount.getUsername());
                                    twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.UNEXPECTED_ERROR);
                                    workingAccounts.remove(twitterAccount.getId());
                                    return;
                                }
                            }
                        }
                        userData = getUserByScreenName(twitterAccount);
                        twitterAccount = twitterAccountService.get(twitterAccountId);
                        updateAccountStatistics(twitterAccountId, userData, twitterAccount, friendsBefore, messagesBefore, retweetsBefore);
                        status = twitterAccount.getStatus();
                        Thread.sleep(1920000);
                    } else {
                        groupTryCounter++;
                        Thread.sleep(600000);
                        if (groupTryCounter > 3) {
                            log.error("Null Groups in execution method : " + twitterAccount.getUsername());
                            status = twitterAccountService.get(twitterAccountId).getStatus();
                            if (!status.equals(TwitterAccountStatus.INVALID_COOKIES) && !status.equals(TwitterAccountStatus.LOCKED)) {
                                twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.UNEXPECTED_ERROR);
                            }
                            workingAccounts.remove(twitterAccount.getId());
                            return;
                        }
                    }
                } catch (XAccountAuthException e) {
                    twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.INVALID_COOKIES);
                    workingAccounts.remove(twitterAccount.getId());
                    return;
                } catch (XAccountLockedException e) {
                    twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.LOCKED);
                    workingAccounts.remove(twitterAccount.getId());
                    return;
                } catch (XAccountRateLimitException | XAccountPermissionException | XAccountOverCapacityException |
                         XAccountCooldownException ignored) {
                } catch (XAccountSuspendedException e) {
                    twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.SUSPENDED);
                    workingAccounts.remove(twitterAccount.getId());
                    return;
                } catch (Exception e) {
                    log.error("Unexpected error in execution method : " + e + " " + twitterAccount.getUsername());
                    status = twitterAccountService.get(twitterAccountId).getStatus();
                    if (status.equals(TwitterAccountStatus.ACTIVE) || status.equals(TwitterAccountStatus.COOLDOWN)) {
                        twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.UNEXPECTED_ERROR);
                        workingAccounts.remove(twitterAccount.getId());
                        return;
                    }
                }

            } else {
                twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.UNPAID);
                workingAccounts.remove(twitterAccount.getId());
                return;
            }
        }
        workingAccounts.remove(twitterAccount.getId());
        if (status.equals(TwitterAccountStatus.STOPPING)) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.DISABLED);
        }
    }

    private int processGroup(TwitterAccount twitterAccount, String groupId, Integer postToRetweet, int retweetCounter) throws InterruptedException {
        XGroupMessage groupMessages = getGroupMessages(twitterAccount, groupId);
        twitterAccount = twitterAccountService.get(twitterAccount.getId());
        TwitterAccountStatus status = twitterAccount.getStatus();
        if (nonNull(groupMessages) && status.equals(TwitterAccountStatus.ACTIVE)) {
            List<Entry> filteredEntries = filterEntries(groupMessages.getConversationTimeline().getEntries(), twitterAccount.getRestId(), postToRetweet);
            List<String> screenNames = convertIdsToScreenNames(filteredEntries, groupMessages.getConversationTimeline().getUsers());
            if (!filteredEntries.isEmpty()) {
                writeMessage(twitterAccount, groupId);
                for (String screenName : screenNames) {
                    try {
                        XUserData userData = twitterApiRequests.getUserByScreenName(screenName, twitterAccount.getProxy(), twitterAccount.getCookie(), twitterAccount.getAuthToken(), twitterAccount.getCsrfToken());
                        retweetPinnedPost(userData, twitterAccount);
                        retweetCounter++;
                    } catch (IndexOutOfBoundsException | XAccountRateLimitException | XAccountPermissionException |
                             XAccountOverCapacityException | SocketTimeoutException | SocketException |
                             XAccountCooldownException ignored) {
                    } catch (XAccountAuthException e) {
                        twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.INVALID_COOKIES);
                        workingAccounts.remove(twitterAccount.getId());
                        break;
                    } catch (XAccountLockedException e) {
                        twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.LOCKED);
                        workingAccounts.remove(twitterAccount.getId());
                        break;
                    } catch (XAccountSuspendedException e) {
                        twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.SUSPENDED);
                        workingAccounts.remove(twitterAccount.getId());
                        break;
                    } catch (XAccountProxyException | ProtocolException e) {
                        twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.PROXY_ERROR);
                        workingAccounts.remove(twitterAccount.getId());
                        break;
                    } catch (Exception e) {
                        log.error("process group error : " + e + " " + twitterAccount.getUsername());
                        twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.UNEXPECTED_ERROR);
                        workingAccounts.remove(twitterAccount.getId());
                        break;
                    }
                }
            }
//            if (!filteredEntries.isEmpty()) {
//                writeMessage(twitterAccount, groupId);
//                try {
//                    filteredEntries.forEach(entry -> retweetUserMedia(entry.getMessage().getMessageData().getSenderId(), twitterAccount));
//                } catch (NullPointerException e) {
//                    log.error("ProcessGroup : nullpointer while retweeting. Account : " + twitterAccount.getUsername());
//                }
//            }
        }
        return retweetCounter;
    }

    private void retweetPinnedPost(XUserData userData, TwitterAccount twitterAccount) {
        String postId = userData.getData().getUser().getResult().getLegacy().getPinnedTweetIdsStr().get(0);
        if (nonNull(postId)) {
            deleteRetweet(twitterAccount, postId);
            retweet(twitterAccount, postId);
        }
    }

    private List<String> convertIdsToScreenNames(List<Entry> entries, Map<String, XUser> users) {
        List<String> screenNames = new ArrayList<>();
        for (Entry entry : entries) {
            MessageData messageData = entry.getMessage().getMessageData();
            String senderId = messageData.getSenderId();
            for (Map.Entry<String, XUser> userEntry : users.entrySet()) {
                XUser xUser = userEntry.getValue();
                if (xUser.getUserId().equals(senderId)) {
                    screenNames.add(xUser.getScreenName());
                }
            }
        }
        return screenNames;
    }

    private List<Entry> filterEntries(List<Entry> entries, String twitterAccountRestId, Integer postToRetweet) {
        try {
            int selfLastMessageIndex = -1;
            Comparator<Entry> comparator = Comparator.comparingLong(entry -> {
                Message msg = entry.getMessage();
                return msg != null ? Long.parseLong(msg.getTime()) : 0;
            });

            List<Entry> sortedList = entries.stream().sorted(comparator).toList();
            for (int i = 0; i < sortedList.size(); i++) {
                Entry current = sortedList.get(i);
                if (nonNull(current.getMessage())) {
                    if (current.getMessage().getMessageData().getSenderId().equals(twitterAccountRestId)) {
                        selfLastMessageIndex = i;
                    }
                }
            }
            if ((sortedList.size() - 1 - selfLastMessageIndex) >= postToRetweet + 1) {
                return sortedList.subList(sortedList.size() - postToRetweet, sortedList.size());
            }
        } catch (NullPointerException ignored) {
        }
        return Collections.emptyList();
    }

    private void retweetUserMedia(String senderId, TwitterAccount twitterAccount) {
        XUserMedia userMedia = getUserMedia(senderId, twitterAccount);
        Set<String> mediaPostId = new HashSet<>();
        try {
            assert userMedia != null;
            for (Instruction instruction : userMedia.getData().getUser().getResult().getTimelineV2().getTimeline().getInstructions()) {
                if (nonNull(instruction.getModuleItems())) {
                    for (ModuleItem moduleItem : instruction.getModuleItems()) {
                        if (nonNull(moduleItem.getItem().getItemContent().getTweetResults().getResult().getLegacy())) {
                            Legacy legacy = moduleItem.getItem().getItemContent().getTweetResults().getResult().getLegacy();
                            if (!legacy.isRetweeted()) {
                                //
                                if (legacy.getRetweetCount() > 15) {
                                    mediaPostId.add(moduleItem.getItem().getItemContent().getTweetResults().getResult().getRestId());
                                    break;
                                }
                            }
                        }
                    }
                } else if (nonNull(instruction.getEntry())) {
                    Legacy legacy = instruction.getEntry().getContent().getItemContent().getTweetResults().getResult().getLegacy();
                    if (!legacy.isRetweeted()) {
                        //
                        if (legacy.getRetweetCount() > 15) {
                            mediaPostId.add(instruction.getEntry().getContent().getItemContent().getTweetResults().getResult().getRestId());
                            break;
                        }
                    }
                } else if (nonNull(instruction.getEntries())) {
                    for (TimelineEntry entry : instruction.getEntries()) {
                        Legacy legacy = entry.getContent().getItemContent().getTweetResults().getResult().getLegacy();
                        if (!legacy.isRetweeted()) {
                            //
                            if (legacy.getRetweetCount() > 15) {
                                mediaPostId.add(entry.getContent().getItemContent().getTweetResults().getResult().getRestId());
                                break;
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException ignored) {
            log.error("RetweetUserMedia nullpointer exception, in account : " + twitterAccount.getUsername());
        }
        if (!mediaPostId.isEmpty()) {
            List<String> mediaPostIdList = new ArrayList<>(mediaPostId);
            retweet(twitterAccount, mediaPostIdList.get(0));
        }
    }

    private XUserData getUserByScreenName(TwitterAccount twitterAccount) {
        try {
            return twitterApiRequests.getUserByScreenName(twitterAccount.getUsername(), twitterAccount.getProxy(), twitterAccount.getCookie(), twitterAccount.getAuthToken(), twitterAccount.getCsrfToken());
        } catch (XAccountAuthException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.INVALID_COOKIES);
//            twitterAuthService.login(twitterAccount);
        } catch (XAccountLockedException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.LOCKED);
        } catch (XAccountRateLimitException | XAccountPermissionException | XAccountOverCapacityException |
                 SocketTimeoutException | SocketException | XAccountCooldownException ignored) {
        } catch (XAccountSuspendedException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.SUSPENDED);
        } catch (XAccountProxyException | ProtocolException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.PROXY_ERROR);
        } catch (Exception e) {
            log.error("getUserByScreenName error : " + twitterAccount.getUsername());
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.UNEXPECTED_ERROR);
        }
        return null;
    }

    private XUserGroup getUserConversations(TwitterAccount twitterAccount) {
        try {
            return twitterApiRequests.getUserConversations(twitterAccount.getUsername(), twitterAccount.getRestId(), twitterAccount.getProxy(), twitterAccount.getCookie(), twitterAccount.getAuthToken(), twitterAccount.getCsrfToken());
        } catch (XAccountAuthException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.INVALID_COOKIES);
//            twitterAuthService.login(twitterAccount);
        } catch (XAccountLockedException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.LOCKED);
        } catch (XAccountRateLimitException | XAccountPermissionException | XAccountOverCapacityException |
                 SocketTimeoutException | SocketException | XAccountCooldownException ignored) {
        } catch (XAccountSuspendedException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.SUSPENDED);
        } catch (XAccountProxyException | ProtocolException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.PROXY_ERROR);
        } catch (Exception e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.UNEXPECTED_ERROR);
        }
        return null;
    }

    private XUserMedia getUserMedia(String sederId, TwitterAccount twitterAccount) {
        try {
            return twitterApiRequests.getUserMedia(twitterAccount.getUsername(), sederId, twitterAccount.getProxy(), twitterAccount.getCookie(), twitterAccount.getAuthToken(), twitterAccount.getCsrfToken());
        } catch (XAccountAuthException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.INVALID_COOKIES);
//            twitterAuthService.login(twitterAccount);
        } catch (XAccountLockedException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.LOCKED);
        } catch (XAccountRateLimitException | XAccountPermissionException | XAccountOverCapacityException |
                 SocketTimeoutException | SocketException | XAccountCooldownException ignored) {
        } catch (XAccountSuspendedException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.SUSPENDED);
        } catch (XAccountProxyException | ProtocolException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.PROXY_ERROR);
        } catch (Exception e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.UNEXPECTED_ERROR);
        }
        return null;
    }

    private XGroupMessage getGroupMessages(TwitterAccount twitterAccount, String groupId) {
        try {
            return twitterApiRequests.getGroupMessages(
                    twitterAccount.getUsername(),
                    groupId, twitterAccount.getProxy(),
                    twitterAccount.getCookie(), twitterAccount.getAuthToken(),
                    twitterAccount.getCsrfToken());
        } catch (XAccountAuthException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.INVALID_COOKIES);
//            twitterAuthService.login(twitterAccount);
        } catch (XAccountLockedException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.LOCKED);
        } catch (XAccountRateLimitException | XAccountPermissionException | XAccountOverCapacityException |
                 SocketTimeoutException | SocketException | XAccountCooldownException ignored) {
        } catch (XAccountSuspendedException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.SUSPENDED);
        } catch (XAccountProxyException | ProtocolException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.PROXY_ERROR);
        } catch (Exception e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.UNEXPECTED_ERROR);
        }
        return null;
    }

    private void writeMessage(TwitterAccount twitterAccount, String groupId) throws InterruptedException {
        TwitterChatMessage chatMessage = getRandomChatMessage(twitterAccount.getMessages());
        String mediaId = getGifMediaId(twitterAccount, chatMessage);
        try {
            twitterApiRequests.writeMessage(twitterAccount.getUsername(), chatMessage.getText(), groupId, twitterAccount.getProxy(), twitterAccount.getCookie(), twitterAccount.getAuthToken(), twitterAccount.getCsrfToken(), mediaId);
            twitterAccountService.updateSentMessages(twitterAccount.getId());
        } catch (XAccountAuthException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.INVALID_COOKIES);
//            twitterAuthService.login(twitterAccount);
        } catch (XAccountLockedException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.LOCKED);
        } catch (XAccountRateLimitException | XAccountPermissionException | XAccountOverCapacityException |
                 SocketTimeoutException | SocketException ignored) {
        } catch (XAccountCooldownException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.COOLDOWN);
            Thread.sleep(generateRandomNumber(3600000, 5400000));
        } catch (XAccountSuspendedException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.SUSPENDED);
        } catch (XAccountProxyException | ProtocolException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.PROXY_ERROR);
        } catch (Exception e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.UNEXPECTED_ERROR);
        }
    }

    private void retweet(TwitterAccount twitterAccount, String postId) {
        try {
            twitterApiRequests.retweet(twitterAccount.getUsername(), postId, twitterAccount.getProxy(), twitterAccount.getCookie(), twitterAccount.getAuthToken(), twitterAccount.getCsrfToken());
        } catch (XAccountAuthException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.INVALID_COOKIES);
//            twitterAuthService.login(twitterAccount);
        } catch (XAccountLockedException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.LOCKED);
        } catch (XAccountRateLimitException | XAccountPermissionException | XAccountOverCapacityException |
                 SocketTimeoutException | SocketException | XAccountCooldownException ignored) {
        } catch (XAccountSuspendedException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.SUSPENDED);
        } catch (XAccountProxyException | ProtocolException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.PROXY_ERROR);
        } catch (Exception e) {
//            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.UNEXPECTED_ERROR);
        }
    }

    private void deleteRetweet(TwitterAccount twitterAccount, String postId) {
        try {
            twitterApiRequests.deleteRetweet(twitterAccount.getUsername(), postId, twitterAccount.getProxy(), twitterAccount.getCookie(), twitterAccount.getAuthToken(), twitterAccount.getCsrfToken());
        } catch (XAccountAuthException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.INVALID_COOKIES);
//            twitterAuthService.login(twitterAccount);
        } catch (XAccountLockedException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.LOCKED);
        } catch (XAccountRateLimitException | XAccountPermissionException | XAccountOverCapacityException |
                 SocketTimeoutException | SocketException | XAccountCooldownException ignored) {
        } catch (XAccountSuspendedException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.SUSPENDED);
        } catch (XAccountProxyException | ProtocolException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.PROXY_ERROR);
        } catch (Exception e) {
//            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.UNEXPECTED_ERROR);
        }
    }

    private String getGifMediaId(TwitterAccount twitterAccount, TwitterChatMessage message) throws InterruptedException {
        String gifUrl = message.getGifUrl();
        if (nonNull(gifUrl)) {
            if (gifUrl.length() > 5) {
                try {
                    XGif gif = twitterApiRequests.getGifMediaId(twitterAccount, gifUrl);
                    for (int i = 0; i < 5; i++) {
                        Thread.sleep(5000);
                        XGifStatus status = twitterApiRequests.checkGifStatus(twitterAccount, gif.getMediaIdString());
                        if (status.getProcessingInfo().getState().equals("succeeded")) {
                            return gif.getMediaIdString();
                        }
                    }
                } catch (IOException e) {
                    log.error("Gif Media id didn't receive : " + twitterAccount.getUsername());
                }
            }
        }
        return null;
    }

    private TwitterChatMessage getRandomChatMessage(List<TwitterChatMessage> messages) {
        if (nonNull(messages)) {
            Random rand = new Random();
            return messages.get(rand.nextInt(messages.size()));
        } else {
            throw new RuntimeException("No available messages exist");
        }
    }

    private long generateRandomNumber(long minValue, long maxValue) {
        if (minValue >= maxValue) {
            throw new IllegalArgumentException("Invalid range: minValue must be less than maxValue.");
        }
        Random random = new Random();
        return random.nextLong(maxValue - minValue + 1) + minValue;
    }

    private boolean isNotExpired(TwitterAccount twitterAccount) {
        LocalDateTime validTo = twitterAccount.getPayedTo();
        LocalDateTime now = LocalDateTime.now();
        long result = now.until(validTo, ChronoUnit.DAYS);
        return result >= 0;
    }

    private Integer groupPostToRetweetParser(String name) {
        Pattern pattern = Pattern.compile("(\\d+)/(\\d+)");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            String fractionAfterSlash = matcher.group(2);
            try {
                return Integer.parseInt(fractionAfterSlash);
            } catch (NumberFormatException ignored) {
            }
        }
        return 3;
    }

    private void updateGroupsValue(XUserGroup groups, Long twitterAccountId) {
        if (nonNull(groups)) {
            int counter = 0;
            for (Conversation conversation : groups.getInboxInitialState().getConversations().values()) {
                if (nonNull(conversation.getName())) {
                    counter++;
                }
            }
            twitterAccountService.updateGroups(twitterAccountId, counter);
        }
    }

    private void updateAccountStatistics(Long twitterAccountId, XUserData userData, TwitterAccount twitterAccount, int friendsBefore, int messagesBefore, int retweetsBefore) {
        if (nonNull(userData)) {
            int friendsAfter = userData.getData().getUser().getResult().getLegacy().getFriendsCount();
            int retweetsAfter = userData.getData().getUser().getResult().getLegacy().getStatusesCount();
            int messagesAfter = twitterAccount.getMessagesSent();
            int friendsDifference = friendsAfter - friendsBefore;
            int messagesDifference = messagesAfter - messagesBefore;
            int retweetDifference = retweetsAfter - retweetsBefore;
            twitterAccountService.updateStatisticDifference(twitterAccountId, friendsDifference, messagesDifference, retweetDifference, friendsAfter, retweetsAfter);
        }
    }
}

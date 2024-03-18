package com.twittersfs.server.services.twitter.app.commands;

import com.twittersfs.server.dtos.twitter.group.Conversation;
import com.twittersfs.server.dtos.twitter.group.XUserGroup;
import com.twittersfs.server.dtos.twitter.media.*;
import com.twittersfs.server.dtos.twitter.message.Entry;
import com.twittersfs.server.dtos.twitter.message.Message;
import com.twittersfs.server.dtos.twitter.message.XGroupMessage;
import com.twittersfs.server.dtos.twitter.user.XUserData;
import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.entities.TwitterChatMessage;
import com.twittersfs.server.enums.TwitterAccountStatus;
import com.twittersfs.server.exceptions.twitter.*;
import com.twittersfs.server.services.TwitterAccountService;
import com.twittersfs.server.services.twitter.auth.TwitterAuthService;
import com.twittersfs.server.services.twitter.readonly.TwitterApiRequests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final TwitterAuthService twitterAuthService;
    Set<Long> workingAccounts = ConcurrentHashMap.newKeySet();

    public TwitterCommandsServiceImpl(TwitterApiRequests twitterApiRequests, TwitterAccountService twitterAccountService, TwitterAuthService twitterAuthService) {
        this.twitterApiRequests = twitterApiRequests;
        this.twitterAccountService = twitterAccountService;
        this.twitterAuthService = twitterAuthService;
    }

    @Override
    public void stop(Long twitterAccountId) {
        if (!workingAccounts.isEmpty()) {
            for (Long id : workingAccounts) {
                if (Objects.equals(id, twitterAccountId)) {
                    twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.STOPPING);
                }
            }
        } else {
            twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.DISABLED);
        }
    }

    public void exec(Long twitterAccountId){

    }

    @Override
    public void execute(Long twitterAccountId) {
        checkIfAccountRunning(twitterAccountId);
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
        int tryCounter = 0;
        while (status.equals(TwitterAccountStatus.ACTIVE) || status.equals(TwitterAccountStatus.COOLDOWN) || status.equals(TwitterAccountStatus.UPDATED_COOKIES)) {
            if (isNotExpired(twitterAccount)) {
                    try {
                        if (status.equals(TwitterAccountStatus.COOLDOWN) || status.equals(TwitterAccountStatus.UPDATED_COOKIES)) {
                            twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.ACTIVE);
                        }
                        twitterAccount = twitterAccountService.get(twitterAccountId);
                        XUserGroup groups = getUserConversations(twitterAccount);
                        int friendsBefore = twitterAccount.getFriends();
                        int messagesBefore = twitterAccount.getMessagesSent();
                        int retweetsBefore = twitterAccount.getRetweets();
                        if (nonNull(groups)) {
                            int processGroupCounter = 0;
                            updateGroupsValue(groups, twitterAccountId);
                            for (Conversation conversation : groups.getInboxInitialState().getConversations().values()) {
                                if (nonNull(conversation.getName())) {
                                    try {
                                        tryCounter = 0;
                                        processGroup(twitterAccount, conversation.getConversationID(), groupPostToRetweetParser(conversation.getName()));
                                        processGroupCounter++;
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
                            if (processGroupCounter <= 5) {
                                Thread.sleep(generateRandomNumber(540000, 600000));
                            } else if (processGroupCounter <= 10) {
                                Thread.sleep(generateRandomNumber(1080000, 1320000));
                            } else if (processGroupCounter <= 15) {
                                Thread.sleep(generateRandomNumber(1740000, 1980000));
                            } else {
                                Thread.sleep(2100000);
                            }
                        } else {
                            tryCounter++;
                            Thread.sleep(60000);
                            if (tryCounter > 10) {
                                log.warn("Null Groups in execution method : " + twitterAccount.getUsername());
                                twitterAccountService.updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.UNEXPECTED_ERROR);
                                workingAccounts.remove(twitterAccount.getId());
                                return;
                            }
                        }
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

    private void processGroup(TwitterAccount twitterAccount, String groupId, Integer postToRetweet) throws InterruptedException {
        XGroupMessage groupMessages = getGroupMessages(twitterAccount, groupId);
        if (nonNull(groupMessages)) {
            List<Entry> filteredEntries = filterEntries(groupMessages.getConversationTimeline().getEntries(), twitterAccount.getRestId(), postToRetweet);
            if (!filteredEntries.isEmpty()) {
                writeMessage(twitterAccount, groupId);
                try {
                    filteredEntries.forEach(entry -> retweetUserMedia(entry.getMessage().getMessageData().getSenderId(), twitterAccount));
                } catch (NullPointerException e) {
                    log.error("ProcessGroup : nullpointer while retweeting. Account : " + twitterAccount.getUsername());
                }
            }
        }
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
        String message = getRandomChatMessage(twitterAccount.getMessages());
        try {
            twitterApiRequests.writeMessage(twitterAccount.getUsername(), message, groupId, twitterAccount.getProxy(), twitterAccount.getCookie(), twitterAccount.getAuthToken(), twitterAccount.getCsrfToken());
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
            Thread.sleep(generateRandomNumber(1200000, 1500000));
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

    private String getRandomChatMessage(List<TwitterChatMessage> messages) {
        if (nonNull(messages)) {
            List<String> groupMessages = new ArrayList<>();
            for (TwitterChatMessage message : messages) {
                groupMessages.add(message.getText());
            }
            Random rand = new Random();
            return groupMessages.get(rand.nextInt(groupMessages.size()));
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
            log.info("Friends after : " + friendsAfter + " account : " + twitterAccount.getUsername());
            int retweetsAfter = userData.getData().getUser().getResult().getLegacy().getStatusesCount();
            int messagesAfter = twitterAccount.getMessagesSent();
            int friendsDifference = friendsAfter - friendsBefore;
            log.info("Friends dif : " + friendsDifference + " account : " + twitterAccount.getUsername());
            int messagesDifference = messagesAfter - messagesBefore;
            int retweetDifference = retweetsAfter - retweetsBefore;
            twitterAccountService.updateStatisticDifference(twitterAccountId, friendsDifference, messagesDifference, retweetDifference, friendsAfter, retweetsAfter);
        }
    }

    private void checkIfAccountRunning(Long accountId) {
        if (!workingAccounts.isEmpty()) {
            for (Long id : workingAccounts) {
                if (Objects.equals(id, accountId)) {
                    throw new RuntimeException("Account is already working");
                }
            }
        }
    }
}

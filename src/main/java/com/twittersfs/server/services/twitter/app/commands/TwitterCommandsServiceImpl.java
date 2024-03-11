package com.twittersfs.server.services.twitter.app.commands;

import com.twittersfs.server.dtos.twitter.media.*;
import com.twittersfs.server.dtos.twitter.message.Entry;
import com.twittersfs.server.dtos.twitter.message.Message;
import com.twittersfs.server.dtos.twitter.message.XGroupMessage;
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
import java.util.*;

import static java.util.Objects.nonNull;

@Service
@Slf4j
public class TwitterCommandsServiceImpl implements TwitterCommandsService {
    private final TwitterApiRequests twitterApiRequests;
    private final TwitterAccountService twitterAccountService;

    public TwitterCommandsServiceImpl(TwitterApiRequests twitterApiRequests, TwitterAccountService twitterAccountService) {
        this.twitterApiRequests = twitterApiRequests;
        this.twitterAccountService = twitterAccountService;
    }

    private void processGroup(TwitterAccount twitterAccount, String groupId, Integer postToRetweet) throws InterruptedException {
        XGroupMessage groupMessages = getGroupMessages(twitterAccount, groupId);
        if (nonNull(groupMessages)) {
            List<Entry> filteredEntries = filterEntries(groupMessages.getConversationTimeline().getEntries(), twitterAccount.getRestId(), postToRetweet);
            if (!filteredEntries.isEmpty()) {
                writeMessage(twitterAccount, groupId);
                filteredEntries.forEach(entry -> retweetUserMedia(entry.getMessage().getMessageData().getSenderId(), twitterAccount));
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
                                mediaPostId.add(moduleItem.getItem().getItemContent().getTweetResults().getResult().getRestId());
                                break;
                            }
                        }
                    }
                } else if (nonNull(instruction.getEntry())) {
                    Legacy legacy = instruction.getEntry().getContent().getItemContent().getTweetResults().getResult().getLegacy();
                    if (!legacy.isRetweeted()) {
                        mediaPostId.add(instruction.getEntry().getContent().getItemContent().getTweetResults().getResult().getRestId());
                        break;
                    }
                } else if (nonNull(instruction.getEntries())) {
                    for (TimelineEntry entry : instruction.getEntries()) {
                        Legacy legacy = entry.getContent().getItemContent().getTweetResults().getResult().getLegacy();
                        if (!legacy.isRetweeted()) {
                            mediaPostId.add(entry.getContent().getItemContent().getTweetResults().getResult().getRestId());
                            break;
                        }
                    }
                }
            }
        } catch (NullPointerException ignored) {}
        if (!mediaPostId.isEmpty()) {
            List<String> mediaPostIdList = new ArrayList<>(mediaPostId);
            retweet(twitterAccount, mediaPostIdList.get(0));
        }
    }

    private XUserMedia getUserMedia(String sederId, TwitterAccount twitterAccount) {
        try {
            return twitterApiRequests.getUserMedia(twitterAccount.getUsername(), sederId, twitterAccount.getProxy(), twitterAccount.getCookie(), twitterAccount.getAuthToken(), twitterAccount.getCsrfToken());
        } catch (XAccountAuthException e) {
            twitterAccountService.updateTwitterAccountStatus(twitterAccount.getId(), TwitterAccountStatus.INVALID_COOKIES);
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
}

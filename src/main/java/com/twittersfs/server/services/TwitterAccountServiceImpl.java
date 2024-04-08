package com.twittersfs.server.services;

import com.twittersfs.server.constants.AppConstant;
import com.twittersfs.server.dtos.common.PageableResponse;
import com.twittersfs.server.dtos.twitter.account.TwitterAccountCreate;
import com.twittersfs.server.dtos.twitter.account.TwitterAccountData;
import com.twittersfs.server.dtos.twitter.account.TwitterAccountUpdate;
import com.twittersfs.server.dtos.twitter.group.Conversation;
import com.twittersfs.server.dtos.twitter.group.XUserGroup;
import com.twittersfs.server.dtos.twitter.message.TwitterChatMessageData;
import com.twittersfs.server.dtos.twitter.message.TwitterChatMessageDto;
import com.twittersfs.server.dtos.twitter.statistic.XAccountStatistic;
import com.twittersfs.server.dtos.twitter.statistic.XAccountTimeZone;
import com.twittersfs.server.dtos.twitter.statistic.XStatistic;
import com.twittersfs.server.dtos.twitter.user.XUserData;
import com.twittersfs.server.entities.*;
import com.twittersfs.server.enums.*;
import com.twittersfs.server.exceptions.user.NotEnoughFunds;
import com.twittersfs.server.okhttp3.OkHttp3ClientService;
import com.twittersfs.server.repos.*;
import com.twittersfs.server.services.twitter.app.commands.AppGroupService;
import com.twittersfs.server.services.twitter.readonly.TwitterApiRequests;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
@Slf4j
public class TwitterAccountServiceImpl implements TwitterAccountService {
    private final TwitterAccountRepo twitterAccountRepo;
    private final TwitterChatMessageRepo messageRepo;
    private final ProxyRepo proxyRepo;
    private final ModelRepo modelRepo;
    private final UserEntityRepo userRepo;
    private final AppGroupService groupService;
    private final TwitterApiRequests apiRequests;
    private final OkHttp3ClientService clientService;

    public TwitterAccountServiceImpl(TwitterAccountRepo twitterAccountRepo, TwitterChatMessageRepo messageRepo, ProxyRepo proxyRepo, ModelRepo modelRepo, UserEntityRepo userRepo, AppGroupService groupService, TwitterApiRequests apiRequests, OkHttp3ClientService clientService) {
        this.twitterAccountRepo = twitterAccountRepo;
        this.messageRepo = messageRepo;
        this.proxyRepo = proxyRepo;
        this.modelRepo = modelRepo;
        this.userRepo = userRepo;
        this.groupService = groupService;
        this.apiRequests = apiRequests;
        this.clientService = clientService;
    }

    @Override
    @Transactional
    public void createTwitterAccount(String email, Long modelId, TwitterAccountCreate dto) throws UnknownHostException {
        UserEntity user = userRepo.findByEmail(email);
        if (user.getBalance() >= Prices.X_DAY_SUBSCRIPTION.getValue()) {
            Proxy proxyToSave = parseProxy(dto.getProxy());
            Optional<Proxy> prx = proxyRepo.findByIpAndPort(proxyToSave.getIp(), proxyToSave.getPort());

            if (prx.isEmpty()) {
                proxyRepo.save(proxyToSave);
            } else {
                throw new RuntimeException("Proxy is used by another account");
            }

            Proxy proxy = proxyRepo.findByIpAndPort(proxyToSave.getIp(), proxyToSave.getPort())
                    .orElseThrow(() -> new RuntimeException("Proxy with such ip and port does not exist"));
            ModelEntity modelEntity = modelRepo.findById(modelId)
                    .orElseThrow(() -> new RuntimeException("Model with such id does not exist"));

            Optional<TwitterAccount> twitterAccount = twitterAccountRepo
                    .findByUsername(parseTwitterUsername(dto.getUsername()));
            if (twitterAccount.isEmpty()) {
                twitterAccountRepo.save(fromTwitterCreate(proxy, modelEntity, dto));
            } else {
                throw new RuntimeException("Twitter account with such username already exists");
            }

            TwitterAccount account = twitterAccountRepo.findByUsername(parseTwitterUsername(dto.getUsername()))
                    .orElseThrow(() -> new RuntimeException("Twitter account with such username does not exist"));
            messageRepo.save(toChatMessageEntity(dto.getMessage(), dto.getGifUrl(), account));
            Float balance = user.getBalance();
            user.setBalance(balance - Prices.X_DAY_SUBSCRIPTION.getValue());
            userRepo.save(user);
            try {
                OkHttpClient client = clientService.createClientWithProxy(account.getProxy());
                XUserData userData = apiRequests.getUserByScreenName(client, account.getUsername(), account.getProxy(), account.getCookie(), account.getAuthToken(), account.getCsrfToken());
                String restId = userData.getData().getUser().getResult().getRestId();
                updateRestId(account.getId(), restId);
                if (user.getSubscriptionType().equals(SubscriptionType.DONOR)) {
                    groupService.addGroupsToADonorAccount(account, restId);
                } else if (user.getSubscriptionType().equals(SubscriptionType.AGENCY)) {
                    int groups = countGroupsAmount(account, restId, client);
                    if (groups < 5) {
                        groupService.addGroupsToAgencyAccount(account, restId);
                    }
                }
                int counter = countGroupsAmount(account, restId, client);
                updateGroups(account.getId(), counter);
            } catch (Exception e) {
                log.error("Exception while getting user data during creating twitter account : " + account.getUsername());
            }

        } else {
            throw new NotEnoughFunds("Not enough funds, top up your balance");
        }
    }

    @Override
    public void createTwitterAccountBulk(String email, Long modelId, List<TwitterAccountCreate> dtos) {
        for (TwitterAccountCreate dto : dtos) {
            try {
                createTwitterAccount(email, modelId, dto);
            } catch (NotEnoughFunds e) {
                throw new RuntimeException("Not enough funds, top up your balance");
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    @Transactional
    public void updateTwitterAccount(Long twitterAccountId, TwitterAccountUpdate dto) throws UnknownHostException {
        if (nonNull(dto.getProxy())) {
            updateProxy(twitterAccountId, dto.getProxy());
        }
        if (nonNull(dto.getUsername())) {
            String username = parseTwitterUsername(dto.getUsername());
            Optional<TwitterAccount> twitterAccount = twitterAccountRepo
                    .findByUsername(parseTwitterUsername(username));
            if (twitterAccount.isEmpty()) {
                twitterAccountRepo.updateUsername(twitterAccountId, username);
            } else {
                throw new RuntimeException("Twitter account with such username already exists");
            }
        }
        if (nonNull(dto.getEmail())) {
            String email = dto.getEmail().toLowerCase().trim();
            twitterAccountRepo.updateEmail(twitterAccountId, email);
        }
        if (nonNull(dto.getPassword())) {
            twitterAccountRepo.updatePasswordById(twitterAccountId, dto.getPassword());
        }
        if (nonNull(dto.getAuthToken()) && !nonNull(dto.getCsrfToken())) {
            throw new RuntimeException("Token and Ct0 should be updated together");
        }
        if (nonNull(dto.getCsrfToken()) && !nonNull(dto.getAuthToken())) {
            throw new RuntimeException("Token and Ct0 should be updated together");
        }

        if (nonNull(dto.getCsrfToken())) {
            twitterAccountRepo.updateCsrfToken(twitterAccountId, dto.getCsrfToken());
            twitterAccountRepo.updateCookie(twitterAccountId, toCookie(dto.getCsrfToken(), dto.getAuthToken()));
            updateTwitterAccountStatus(twitterAccountId, TwitterAccountStatus.DISABLED);
        }
    }

    @Override
    public void updateTwitterAccountStatus(Long twitterAccountId, TwitterAccountStatus status) {
        twitterAccountRepo.updateStatus(twitterAccountId, status);
    }

    @Override
    public void updateSentMessages(Long twitterAccountId) {
        twitterAccountRepo.updateMessagesSent(twitterAccountId);
    }

    @Override
    @Transactional
    public void deleteProxyFromTwitterAccount(Long twitterAccountId) {
        TwitterAccount account = twitterAccountRepo.findById(twitterAccountId)
                .orElseThrow(() -> new RuntimeException("Twitter account wish such Id does not exist"));
        Proxy old = account.getProxy();
        account.setProxy(null);
        proxyRepo.delete(old);
        twitterAccountRepo.save(account);
    }

    @Override
    @Transactional
    public void deleteTwitterAccount(Long twitterAccountId) {
        TwitterAccount account = twitterAccountRepo.findById(twitterAccountId)
                .orElseThrow(() -> new RuntimeException("Twitter account wish such Id does not exist"));
        LocalDateTime payedTo = account.getPayedTo();
        Integer daysRemaining = calculateDaysRemaining(payedTo);
        Float newBalance = daysRemaining * Prices.X_DAY_SUBSCRIPTION.getValue();
        UserEntity user = account.getModel().getUser();
        user.setBalance(user.getBalance() + newBalance);
        userRepo.save(user);
        twitterAccountRepo.delete(account);
    }

    @Override
    @Transactional
    public void updateSubscription(Long twitterAccountId, Integer month) {
        TwitterAccount twitterAccount = twitterAccountRepo.findById(twitterAccountId)
                .orElseThrow(() -> new RuntimeException("Twitter account wish such Id does not exist"));
        if (nonNull(twitterAccount.getPayedTo())) {
            twitterAccount.setPayedTo(setExpirationDate(twitterAccount.getPayedTo(), month));
        } else {
            twitterAccount.setPayedTo(setExpirationDate(LocalDateTime.now(), month));
        }
        UserEntity user = twitterAccount.getModel().getUser();
        Float toPay = null;
        switch (month) {
            case 1 -> toPay = Prices.X_MONTH_SUBSCRIPTION.getValue();
            case 2 -> toPay = Prices.X_2MONTH_SUBSCRIPTION.getValue();
            case 3 -> toPay = Prices.X_3MONTH_SUBSCRIPTION.getValue();
            case 6 -> toPay = Prices.X_6MONTH_SUBSCRIPTION.getValue();
            case 12 -> toPay = Prices.X_12MONTH_SUBSCRIPTION.getValue();
        }
        if (nonNull(toPay)) {
            if (user.getBalance() >= toPay) {
                user.setBalance(user.getBalance() - toPay);
                userRepo.save(user);
                twitterAccountRepo.save(twitterAccount);
            } else {
                throw new RuntimeException("Not enough funds, refill your balance");
            }
        }
    }

    @Override
    @Transactional
    public void addChatMessage(Long twitterAccountId, TwitterChatMessageDto dto) {
        TwitterAccount account = twitterAccountRepo.findById(twitterAccountId)
                .orElseThrow(() -> new RuntimeException("Twitter account wish such Id does not exist"));
        messageRepo.save(toChatMessageEntity(dto.getMessage(), dto.getGifUrl(), account));
    }

    @Override
    @Transactional
    public void deleteChatMessage(Long messageId) {
        TwitterChatMessage message = messageRepo.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Chat message with such Id does not exist"));
        message.setTwitterAccount(null);
        messageRepo.delete(message);
    }

    @Override
    public TwitterAccount get(Long twitterAccountId) {
        return twitterAccountRepo.findById(twitterAccountId).orElseThrow(() -> new RuntimeException("Twitter account with such Id does not exist"));
    }

    @Override
    public void updateRestId(Long twitterAccountId, String restId) {
        twitterAccountRepo.updateRestId(twitterAccountId, restId);
    }

    @Override
    public void updateGroups(Long twitterAccountId, Integer groups) {
        twitterAccountRepo.updateGroups(twitterAccountId, groups);
    }

    @Override
    public void updateStatisticDifference(Long twitterAccountId, Integer friendDifference, Integer messageDifference, Integer retweetDifference, Integer friends, Integer retweets) {
        twitterAccountRepo.updateAccountFields(twitterAccountId, friends, retweets, friendDifference, retweetDifference, messageDifference);
    }

    @Override
    public void setMessagesDifferenceViewed(Long twitterAccountId) {
        twitterAccountRepo.resetMessagesDifference(twitterAccountId);
    }

    @Override
    public void setRetweetsDifferenceViewed(Long twitterAccountId) {
        twitterAccountRepo.resetRetweetsDifference(twitterAccountId);
    }

    @Override
    public void setFriendsDifferenceViewed(Long twitterAccountId) {
        twitterAccountRepo.resetFriendsDifference(twitterAccountId);
    }

    @Override
    public List<TwitterAccount> findAll() {
        return twitterAccountRepo.findAll();
    }

    @Override
    public PageableResponse<TwitterAccountData> getFilteredTwitterAccounts(String email, TwitterAccountStatus status, int page, int size) {
        Page<TwitterAccount> accounts;
        if (status.equals(TwitterAccountStatus.ALL)) {
            accounts = twitterAccountRepo.findByModel_User_Email(email, PageRequest.of(page, size));
        } else {
            accounts = twitterAccountRepo.findByStatusAndModelUserEmail(status, email, PageRequest.of(page, size));
        }
        return PageableResponse.<TwitterAccountData>builder()
                .totalPages(accounts.getTotalPages())
                .totalElements(accounts.getTotalElements())
                .elements(filteredTwitterAccountList(accounts.getContent()))
                .build();
    }

    @Override
    public PageableResponse<TwitterAccountData> getTwitterAccountsByModel(Long modelId, int page, int size) {
        Page<TwitterAccount> accounts = twitterAccountRepo.findByModel_Id(modelId, PageRequest.of(page, size));
        return PageableResponse.<TwitterAccountData>builder()
                .totalPages(accounts.getTotalPages())
                .totalElements(accounts.getTotalElements())
                .elements(filteredTwitterAccountList(accounts.getContent()))
                .build();
    }

    @Override
    public XStatistic getAccountStatistic(Long twitterAccountId) {
        TwitterAccount account = twitterAccountRepo.findById(twitterAccountId)
                .orElseThrow(() -> new RuntimeException("Twitter account with such Id does not exist"));
        try {
            OkHttpClient client = clientService.createClientWithProxy(account.getProxy());
            XAccountStatistic statistic = apiRequests.getAccountStatistic(client, account);
            String adsAccountId = apiRequests.getAdsAccountId(client, account);
            XAccountTimeZone timeZone = apiRequests.getAccountTimeZone(client, account, adsAccountId);
            return XStatistic.builder().xAccountStatistic(statistic).xAccountTimeZone(timeZone).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get account statistic");
        }
    }

    private void updateProxy(Long twitterAccountId, String proxy) throws UnknownHostException {
        TwitterAccount account = twitterAccountRepo.findById(twitterAccountId)
                .orElseThrow(() -> new RuntimeException("Twitter account wish such Id does not exist"));
        if (account.getProxy() == null) {
            Proxy proxyToSave = parseProxy(proxy);
            Optional<Proxy> check = proxyRepo.findByIpAndPort(proxyToSave.getIp(), proxyToSave.getPort());
            if (check.isEmpty()) {
                proxyRepo.save(proxyToSave);
            } else {
                throw new RuntimeException("Proxy is used by another account");
            }
            Proxy entity = proxyRepo.findByIpAndPort(proxyToSave.getIp(), proxyToSave.getPort())
                    .orElseThrow(() -> new RuntimeException("Proxy wish such IP and Port does not exist"));
            account.setProxy(entity);
            twitterAccountRepo.save(account);
        } else {
            Proxy old = account.getProxy();
            account.setProxy(null);
            proxyRepo.delete(old);
            Proxy proxyToSave = parseProxy(proxy);
            Optional<Proxy> check = proxyRepo.findByIpAndPort(proxyToSave.getIp(), proxyToSave.getPort());
            if (check.isEmpty()) {
                proxyRepo.save(proxyToSave);
                Proxy entity = proxyRepo.findByIpAndPort(proxyToSave.getIp(), proxyToSave.getPort())
                        .orElseThrow(() -> new RuntimeException("Proxy wish such IP and Port does not exist"));
                account.setProxy(entity);
                twitterAccountRepo.save(account);
            } else {
                throw new RuntimeException("Proxy is used by another account");
            }
        }
    }

    private TwitterAccount fromTwitterCreate(Proxy proxy, ModelEntity modelEntity, TwitterAccountCreate dto) {
        LocalDateTime now = LocalDateTime.now();
        String auth = dto.getAuthToken().replaceAll("\\s", "");
        String csrf = dto.getCsrfToken().replaceAll("\\s", "");
        String email = dto.getEmail().toLowerCase().trim();
        SubscriptionType type = modelEntity.getUser().getSubscriptionType();
        GroupStatus groupStatus = GroupStatus.USED;
        if (type.equals(SubscriptionType.BASIC)) {
            groupStatus = GroupStatus.UNUSED;
        }
        return TwitterAccount.builder()
                .proxy(proxy)
                .model(modelEntity)
                .email(email)
                .password(dto.getPassword())
                .csrfToken(dto.getCsrfToken())
                .authToken(AppConstant.TWITTER_TOKEN)
                .cookie(toCookie(csrf, auth))
                .payedTo(now.plusDays(1))
                .groupStatus(groupStatus)
                .friendsDifference(0)
                .messagesDifference(0)
                .groups(0)
                .retweetsDifference(0)
                .friends(0)
                .messagesSent(0)
                .retweets(0)
                .username(parseTwitterUsername(dto.getUsername()))
                .status(TwitterAccountStatus.DISABLED)
                .build();
    }

    private List<TwitterAccountData> filteredTwitterAccountList(List<TwitterAccount> entities) {
        return entities.stream().map(this::fromTwitterAccount).collect(Collectors.toList());
    }

    private TwitterAccountData fromTwitterAccount(TwitterAccount entity) {
        return TwitterAccountData.builder()
                .paidTo(entity.getPayedTo().toLocalDate().toString())
                .id(entity.getId())
                .email(entity.getEmail())
                .username(entity.getUsername())
                .model(entity.getModel().getName())
                .password(entity.getPassword())
                .groups(entity.getGroups())
                .friends(entity.getFriends())
                .messages(entity.getMessagesSent())
                .chatMessages(toChatMessageDataList(entity.getMessages()))
                .friendsDifference(entity.getFriendsDifference())
                .retweetsDifference(entity.getRetweetsDifference())
                .messagesDifference(entity.getMessagesDifference())
                .retweets(entity.getRetweets())
                .status(entity.getStatus())
                .proxy(nonNull(entity.getProxy()) ? entity.getProxy().getIp() + ":" + entity.getProxy().getPort() : null)
                .build();
    }

    private List<TwitterChatMessageData> toChatMessageDataList(List<TwitterChatMessage> entities) {
        return entities.stream().map(this::toChatMessageData).collect(Collectors.toList());
    }

    private TwitterChatMessageData toChatMessageData(TwitterChatMessage entity) {
        return TwitterChatMessageData.builder()
                .id(entity.getId())
                .text(entity.getText())
                .gifUrl(entity.getGifUrl())
                .build();
    }

    private TwitterChatMessage toChatMessageEntity(String text, String gifUrl, TwitterAccount account) {
        String url = "";
        if (nonNull(gifUrl)) {
            url = gifUrl;
        }
        return TwitterChatMessage.builder()
                .twitterAccount(account)
                .text(text)
                .gifUrl(url)
                .build();
    }

    private Proxy parseProxy(String proxyStr) throws UnknownHostException {
        String stringWithoutSpaces = proxyStr.replaceAll("\\s", "");
        String proxy = "";
        ProxyType type = ProxyType.HTTP;
        if (stringWithoutSpaces.contains("http")) {
            proxy = stringWithoutSpaces.replace("http://", "");
            type = ProxyType.HTTP;
        } else if (stringWithoutSpaces.contains("socks5")) {
            proxy = stringWithoutSpaces.replace("socks5://", "");
            type = ProxyType.SOCKS;
        }

        String firstPart, secondPart;
        String ip = "", port = "", username = "", password = "";

        if (proxy.contains("@")) {
            String[] proxyParts = proxy.split("@");

            if (proxyParts.length == 2) {
                firstPart = proxyParts[0];
                secondPart = proxyParts[1];
            } else {
                throw new RuntimeException("Failed to parse proxy, invalid parts count");
            }
        } else {
            String[] proxyParts = proxy.split(":");

            if (proxyParts.length == 4) {
                firstPart = proxyParts[0] + ":" + proxyParts[1];
                secondPart = proxyParts[2] + ":" + proxyParts[3];
            } else {
                throw new RuntimeException("Failed to parse proxy, invalid parts count");
            }
        }
        String[] extractResult = extractIPAndPort(firstPart);
        String extractIp = extractResult[0];
        String extractPort = extractResult[1];
        if (!extractIp.isEmpty() && !extractPort.isEmpty()) {
            ip = extractIp;
            port = extractPort;

            String[] credentials = secondPart.split(":");
            if (credentials.length == 2) {
                username = credentials[0];
                password = credentials[1];
            } else {
                throw new RuntimeException("Failed to find credentials");
            }
        } else {
            extractResult = extractIPAndPort(secondPart);
            extractIp = extractResult[0];
            extractPort = extractResult[1];

            if (!extractIp.isEmpty() && !extractPort.isEmpty()) {
                ip = extractIp;
                port = extractPort;
                String[] credentials = firstPart.split(":");
                if (credentials.length == 2) {
                    username = credentials[0];
                    password = credentials[1];
                } else {
                    throw new RuntimeException("Failed to find credentials");
                }
            } else {
                String[] netParts = secondPart.split(":");

                if (netParts.length != 2) {
                    throw new RuntimeException("Failed to parse proxy, invalid parts count");
                }

                InetAddress[] ipAddresses = InetAddress.getAllByName(netParts[0]);
                if (ipAddresses.length == 0) {
                    throw new UnknownHostException("Failed to resolve domain");
                }
                ip = netParts[0];
                port = netParts[1];

                String[] credentials = firstPart.split(":");
                if (credentials.length == 2) {
                    username = credentials[0];
                    password = credentials[1];
                } else {
                    throw new RuntimeException("Failed to find credentials");
                }
            }
        }

        return Proxy.builder()
                .ip(ip)
                .port(port)
                .username(username)
                .password(password)
                .type(type)
                .build();
    }

    public static String[] extractIPAndPort(String input) {
        Pattern pattern = Pattern.compile("@?([\\d.]+):(\\d+)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find() && matcher.groupCount() == 2) {
            return new String[]{matcher.group(1), matcher.group(2)};
        } else {
            return new String[]{"", ""};
        }
    }

    private String parseTwitterUsername(String message) {
        String msgToSave = message.replaceAll("\\s", "");
        return msgToSave.startsWith("@") ? msgToSave.substring(1) : msgToSave;
    }

    private String toCookie(String csrf, String token) {
        return "auth_token=" + token + "; ct0=" + csrf;
    }

    private Integer calculateDaysRemaining(LocalDateTime payedTo) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(payedTo)) {
            return 0;
        } else {
            return (int) ChronoUnit.DAYS.between(now, payedTo);
        }
    }

    private LocalDateTime setExpirationDate(LocalDateTime rested, Integer month) {
        switch (month) {
            case 1 -> {
                return rested.plusDays(30);
            }
            case 2 -> {
                return rested.plusDays(60);
            }
            case 3 -> {
                return rested.plusDays(90);
            }
            case 6 -> {
                return rested.plusDays(180);
            }
            case 12 -> {
                return rested.plusDays(360);
            }
            default -> throw new IllegalStateException("Unexpected value: " + month);
        }
    }

    private int countGroupsAmount(TwitterAccount account, String restId, OkHttpClient client) {
        int counter = 0;
        try {
            XUserGroup userGroup = apiRequests.getUserConversations(client, account.getUsername(), restId, account.getProxy(), account.getCookie(), account.getAuthToken(), account.getCsrfToken());
            for (Conversation conversation : userGroup.getInboxInitialState().getConversations().values()) {
                if (nonNull(conversation.getName())) {
                    counter++;
                }
            }
        } catch (Exception e) {
            log.error("Exception while counting groups amount on account : " + e);
        }
        return counter;
    }
}

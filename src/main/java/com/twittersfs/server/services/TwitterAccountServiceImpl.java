package com.twittersfs.server.services;

import com.twittersfs.server.constants.AppConstant;
import com.twittersfs.server.dtos.twitter.account.TwitterAccountUpdate;
import com.twittersfs.server.entities.*;
import com.twittersfs.server.enums.Prices;
import com.twittersfs.server.enums.TwitterAccountStatus;
import com.twittersfs.server.exceptions.user.NotEnoughFunds;
import com.twittersfs.server.dtos.twitter.account.TwitterAccountCreate;
import com.twittersfs.server.repos.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Service
@Slf4j
public class TwitterAccountServiceImpl implements TwitterAccountService {
    private final TwitterAccountRepo twitterAccountRepo;
    private final TwitterChatMessageRepo messageRepo;
    private final ProxyRepo proxyRepo;
    private final ModelRepo modelRepo;
    private final UserEntityRepo userRepo;

    public TwitterAccountServiceImpl(TwitterAccountRepo twitterAccountRepo, TwitterChatMessageRepo messageRepo, ProxyRepo proxyRepo, ModelRepo modelRepo, UserEntityRepo userRepo) {
        this.twitterAccountRepo = twitterAccountRepo;
        this.messageRepo = messageRepo;
        this.proxyRepo = proxyRepo;
        this.modelRepo = modelRepo;
        this.userRepo = userRepo;
    }

    @Override
    @Transactional
    public void createTwitterAccount(String email, Long modelId, TwitterAccountCreate dto) throws UnknownHostException {
        UserEntity user = userRepo.findByEmail(email);
        if (user.getBalance() >= Prices.X_MONTH_SUBSCRIPTION.getValue()) {
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
                    .findByUsernameAndModel_Id(parseTwitterUsername(dto.getUsername()), modelId);
            if (twitterAccount.isEmpty()) {
                twitterAccountRepo.save(fromTwitterCreate(proxy, modelEntity, dto));
            } else {
                throw new RuntimeException("Twitter account with such username at this model already exists");
            }

            TwitterAccount account = twitterAccountRepo.findByUsernameAndModel_Id(parseTwitterUsername(dto.getUsername()), modelId)
                    .orElseThrow(() -> new RuntimeException("Twitter account with such username at this model does not exist"));
            messageRepo.save(toChatMessageEntity(dto.getMessage(), account));
            Integer balance = user.getBalance();
            user.setBalance(balance - Prices.X_MONTH_SUBSCRIPTION.getValue());
            userRepo.save(user);
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
            twitterAccountRepo.updateUsername(twitterAccountId, username);
        }
        if (nonNull(dto.getEmail())) {
            String email = dto.getEmail().toLowerCase().trim();
            twitterAccountRepo.updateEmail(twitterAccountId, email);
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
    @Transactional
    public void deleteProxyFromTwitterAccount(Long twitterAccountId){
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
        account.setModel(null);
        twitterAccountRepo.save(account);
        twitterAccountRepo.delete(account);
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
        String email = dto.getEmail();
        if (nonNull(dto.getEmail())) {
            email = dto.getEmail().toLowerCase().trim();
        }
        return TwitterAccount.builder()
                .proxy(proxy)
                .model(modelEntity)
                .email(email)
                .csrfToken(dto.getCsrfToken())
                .authToken(AppConstant.TWITTER_TOKEN)
                .cookie(toCookie(csrf, auth))
                .payedTo(now.plusDays(30))
                .username(parseTwitterUsername(dto.getUsername()))
                .status(TwitterAccountStatus.DISABLED)
                .build();
    }

    private TwitterChatMessage toChatMessageEntity(String message, TwitterAccount account) {
        return TwitterChatMessage.builder()
                .twitterAccount(account)
                .text(message)
                .build();
    }

    private Proxy parseProxy(String proxyStr) throws UnknownHostException {
        String stringWithoutSpaces = proxyStr.replaceAll("\\s", "");
        String proxy = "";
        String type = "";
        if (stringWithoutSpaces.contains("http")) {
            proxy = stringWithoutSpaces.replace("http://", "");
            type = "http";
        } else {
            proxy = stringWithoutSpaces.replace("socks://", "");
            type = "socks";
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

    private String[] extractIPAndPort(String input) {
        String[] parts = input.split(":");
        return (parts.length == 2) ? parts : null;
    }

    private String parseTwitterUsername(String message) {
        String msgToSave = message.replaceAll("\\s", "");
        return msgToSave.startsWith("@") ? msgToSave.substring(1) : msgToSave;
    }

    private String toCookie(String csrf, String token) {
        return "auth_token=" + token + "; ct0=" + csrf;
    }
}

package com.twittersfs.server.services.twitter.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.twittersfs.server.dtos.twitter.error.XApiError;
import com.twittersfs.server.entities.Proxy;
import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.enums.TwitterAccountStatus;
import com.twittersfs.server.okhttp3.OkHttp3ClientService;
import com.twittersfs.server.repos.TwitterAccountRepo;
import com.twittersfs.server.services.twitter.auth.enums.ELoginSubtasks;
import com.twittersfs.server.services.twitter.auth.enums.ELoginUrls;
import com.twittersfs.server.services.twitter.auth.models.AccountCredential;
import com.twittersfs.server.services.twitter.auth.models.AuthCredential;
import com.twittersfs.server.services.twitter.auth.models.response.FlowToken;
import com.twittersfs.server.services.twitter.auth.models.response.Subtask;
import com.twittersfs.server.services.twitter.auth.models.response.SubtaskId;
import com.twittersfs.server.services.twitter.auth.models.subtasks.LoginSubtaskPayload;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static java.util.Objects.nonNull;

@Service
@Slf4j
public class TwitterAuthServiceImpl implements TwitterAuthService {
    private String flowToken;
    private AuthCredential cred;
    private final ELoginSubtasks[] subtasks;
    private final TwitterAccountRepo twitterAccountRepo;
    private final OkHttp3ClientService okHttp3ClientService;

    public TwitterAuthServiceImpl(TwitterAccountRepo twitterAccountRepo, OkHttp3ClientService okHttp3ClientService) {
        this.twitterAccountRepo = twitterAccountRepo;
        this.okHttp3ClientService = okHttp3ClientService;
        this.flowToken = "";
        this.cred = new AuthCredential();
        this.subtasks = new ELoginSubtasks[]{
                ELoginSubtasks.JS_INSTRUMENTATION,
                ELoginSubtasks.ENTER_USER_IDENTIFIER,
                ELoginSubtasks.ENTER_ALTERNATE_USER_IDENTIFIER,
                ELoginSubtasks.ENTER_PASSWORD,
                ELoginSubtasks.ACCOUNT_DUPLICATION_CHECK
        };
    }

    @Override
    @Transactional
    public void login(TwitterAccount twitterAccount) throws IOException {
        twitterAccountRepo.updateCookie(twitterAccount.getId(), "null");
        for (int i = 0; i < 5; i++) {
            try {
                getUserCredential(twitterAccount);
                TwitterAccount account = twitterAccountRepo.findById(twitterAccount.getId()).orElseThrow(() -> new RuntimeException("No Acc"));
                String cookies = account.getCookie();
                if (nonNull(cookies)) {
                    if (!cookies.equals("null")) {
                        twitterAccountRepo.updateStatus(twitterAccount.getId(), TwitterAccountStatus.UPDATED_COOKIES);
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Error during logging in " + e + " : " + twitterAccount.getUsername());
            }
        }
    }

    private void getUserCredential(TwitterAccount twitterAccount) throws IOException {
        if (nonNull(twitterAccount.getProxy())) {
            AccountCredential accCred = toAccountCredential(twitterAccount);
            this.cred = getGuestCredential(twitterAccount.getProxy());
            initiateLogin(twitterAccount.getProxy());

            for (int i = 0; i < subtasks.length; i++) {
                LoginSubtaskPayload payload = getSubtaskPayload(subtasks[i], flowToken, accCred);
                OkHttpClient client = okHttp3ClientService.createClientWithProxy(twitterAccount.getProxy());
                try {
                    Response response = executeLoginSubtask(payload, client);
                    String jsonResponse = response.body().string();
                    handleLoginSubtaskResponse(response, i, twitterAccount, jsonResponse);
                } catch (Exception e) {
                    log.error("Error during handling subtasks at account : " + twitterAccount.getUsername());
                }
            }
            twitterAccountRepo.updateCsrfToken(twitterAccount.getId(), this.cred.getCsrfToken());
        } else {
            throw new RuntimeException("Proxy must not be empty");
        }

    }

    private LoginSubtaskPayload getSubtaskPayload(
            ELoginSubtasks subtask,
            String flowToken,
            AccountCredential accCred
    ) {
        return switch (subtask) {
            case ENTER_USER_IDENTIFIER -> new LoginSubtaskPayload(flowToken, subtask, accCred.getEmail());
            case ENTER_ALTERNATE_USER_IDENTIFIER -> new LoginSubtaskPayload(flowToken, subtask, accCred.getUsername());
            case ENTER_PASSWORD -> new LoginSubtaskPayload(flowToken, subtask, accCred.getPassword());
            default -> new LoginSubtaskPayload(flowToken, subtask);
        };
    }

    private void initiateLogin(Proxy proxy) throws IOException {
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        OkHttpClient client = okHttp3ClientService.createClientWithProxy(proxy);
        Request request = new Request.Builder()
                .url(ELoginUrls.INITIATE_LOGIN.getValue())
                .post(RequestBody.create(null, new byte[0])) // assuming an empty request body
                .headers(this.cred.toHeader().getHeaders()) // assuming toHeader() returns a Map<String, String>
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            this.flowToken = mapper.readValue(response.body().string(), FlowToken.class).getFlowToken();
            String[] setCookieHeaders = response.headers("Set-Cookie").toArray(new String[0]);
            String cookies = String.join(";", setCookieHeaders);
            this.cred.setCookies(cookies);
        }
    }

    private AuthCredential getGuestCredential(Proxy proxy) throws IOException {
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        OkHttpClient client = okHttp3ClientService.createClientWithProxy(proxy);
        AuthCredential cred = new AuthCredential();

        Request request = new Request.Builder()
                .url(ELoginUrls.GUEST_TOKEN.getValue())
                .post(RequestBody.create(null, new byte[0]))
                .headers(cred.toHeader().getHeaders())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            String jsonResponse = response.body().string();
            JsonNode jsonNode = mapper.readTree(jsonResponse);
            String guestToken = jsonNode.get("guest_token").asText();
            cred.setGuestToken(guestToken);
        }

        return cred;
    }

    private Response executeLoginSubtask(LoginSubtaskPayload payload, OkHttpClient client) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"), mapper.writeValueAsString(payload));

        Request request = new Request.Builder()
                .url(ELoginUrls.LOGIN_SUBTASK.getValue())
                .post(requestBody)
                .headers(this.cred.toHeader().getHeaders())
                .build();

        return client.newCall(request).execute();
    }

    private void handleLoginSubtaskResponse(Response response, int i, TwitterAccount account, String jsonResponse) throws IOException {
        ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String[] setCookieHeaders = response.headers("Set-Cookie").toArray(new String[0]);

        for (String cookie : setCookieHeaders) {
        }
        String cookies = String.join(";", setCookieHeaders);
        Subtask subtask = mapper.readValue(jsonResponse, Subtask.class);
        if (subtasks[i].equals(ELoginSubtasks.ENTER_USER_IDENTIFIER) &&
                subtask.getSubtasks().stream().map(SubtaskId::getSubtaskId).toList().contains(ELoginSubtasks.ENTER_PASSWORD.getValue())) {
            i++;
        }

        this.flowToken = subtask.getFlowToken();

        if (subtasks[i].equals(ELoginSubtasks.ACCOUNT_DUPLICATION_CHECK)) {
            twitterAccountRepo.updateCookie(account.getId(), cookies);
            this.cred = new AuthCredential(setCookieHeaders, null);
        }
    }

    private AccountCredential toAccountCredential(TwitterAccount entity) {
        return AccountCredential.builder()
                .email(entity.getEmail())
                .username(entity.getUsername())
                .password(entity.getPassword())
                .build();
    }
}

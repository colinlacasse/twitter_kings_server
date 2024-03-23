package com.twittersfs.server.services.twitter.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twittersfs.server.captcha.CaptchaResolver;
import com.twittersfs.server.entities.Proxy;
import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.enums.TwitterAccountStatus;
import com.twittersfs.server.okhttp3.OkHttp3ClientService;
import com.twittersfs.server.repos.TwitterAccountRepo;
import com.twittersfs.server.enums.ELoginSubtasks;
import com.twittersfs.server.enums.ELoginUrls;
import com.twittersfs.server.dtos.twitter.auth.common.AccountCredential;
import com.twittersfs.server.dtos.twitter.auth.common.AuthCredential;
import com.twittersfs.server.dtos.twitter.auth.response.FlowToken;
import com.twittersfs.server.dtos.twitter.auth.response.Subtask;
import com.twittersfs.server.dtos.twitter.auth.response.SubtaskId;
import com.twittersfs.server.dtos.twitter.auth.request.LoginSubtaskPayload;
import com.twittersfs.server.dtos.twitter.auth.unclock.XCaptchaToken;
import com.twittersfs.server.services.twitter.readonly.TwitterApiRequests;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.nonNull;

@Service
//@RequestScope //todo
@Slf4j
public class TwitterAuthServiceImpl implements TwitterAuthService {
    private String flowToken;
    private AuthCredential cred;
    private final ELoginSubtasks[] subtasks;
    private final TwitterAccountRepo twitterAccountRepo;
    private final OkHttp3ClientService okHttp3ClientService;
    private final TwitterApiRequests apiRequests;
    private final CaptchaResolver captchaResolver;
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public TwitterAuthServiceImpl(TwitterAccountRepo twitterAccountRepo, OkHttp3ClientService okHttp3ClientService, TwitterApiRequests apiRequests, CaptchaResolver captchaResolver) {
        this.twitterAccountRepo = twitterAccountRepo;
        this.okHttp3ClientService = okHttp3ClientService;
        this.apiRequests = apiRequests;
        this.captchaResolver = captchaResolver;
        this.flowToken = "";
        this.cred = new AuthCredential();
        this.subtasks = new ELoginSubtasks[]{
                ELoginSubtasks.JS_INSTRUMENTATION,
                ELoginSubtasks.ENTER_USER_IDENTIFIER,
//                ELoginSubtasks.ENTER_ALTERNATE_USER_IDENTIFIER,
                ELoginSubtasks.ENTER_PASSWORD,
                ELoginSubtasks.ACCOUNT_DUPLICATION_CHECK
        };
    }

//    public void newLogin(TwitterAccount twitterAccount) throws IOException {
//        String guestId = apiRequests.getGuestCreds(twitterAccount);
//        InitLogin initLogin = apiRequests.initiateLogin(twitterAccount, guestId);
//        String flowToken = initLogin.getFlowToken();
//        String cookies = initLogin.getCookies();
//        List<ELoginSubtasks> subtasks = getSubtasks();
//        for (ELoginSubtasks subtask : subtasks) {
//            log.info("SUB : " + subtask.getValue());
//            LoginSubtaskPayload request = getSubtaskPayload(subtask, flowToken, twitterAccount);
//            String[] setCookieHeaders;
//            String jsonResponse;
//            try (Response response = apiRequests.postLoginData(twitterAccount, guestId, request, cookies)) {
//                setCookieHeaders = response.headers("Set-Cookie").toArray(new String[0]);
//                jsonResponse = response.body().string();
//                log.info("JS RESP : " + jsonResponse);
//            }
//            cookies = String.join(";", setCookieHeaders);
//            Subtask sbtask = mapper.readValue(jsonResponse, Subtask.class);
//            flowToken = sbtask.getFlowToken();
//            if (subtask.equals(ELoginSubtasks.ACCOUNT_DUPLICATION_CHECK)) {
////                twitterAccountRepo.updateCookie(twitterAccount.getId(), cookies);
//                log.info("Cookies : " + cookies);
//            }
//        }
//    }

//    private List<ELoginSubtasks> getSubtasks() {
//        List<ELoginSubtasks> subtasks = new ArrayList<>();
//        subtasks.add(ELoginSubtasks.JS_INSTRUMENTATION);
//        subtasks.add(ELoginSubtasks.ENTER_USER_IDENTIFIER);
//        subtasks.add(ELoginSubtasks.ENTER_PASSWORD);
//        subtasks.add(ELoginSubtasks.ACCOUNT_DUPLICATION_CHECK);
//        return subtasks;
//    }

//        private LoginSubtaskPayload getSubtaskPayload(
//            ELoginSubtasks subtask,
//            String flowToken,
//            AccountCredential accCred
//    ) {
//        return switch (subtask) {
//            case ENTER_USER_IDENTIFIER -> new LoginSubtaskPayload(flowToken, subtask, accCred.getEmail());
//            case ENTER_ALTERNATE_USER_IDENTIFIER -> new LoginSubtaskPayload(flowToken, subtask, accCred.getUsername());
//            case ENTER_PASSWORD -> new LoginSubtaskPayload(flowToken, subtask, accCred.getPassword());
//            default -> new LoginSubtaskPayload(flowToken, subtask);
//        };
//    }
//    private XLoginRequest getSubtaskPayload(
//            ELoginSubtasks subtask,
//            String flowToken,
//            TwitterAccount twitterAccount
//    ) {
//        return switch (subtask) {
//            case JS_INSTRUMENTATION -> createJsInstrPayload(flowToken, subtask.getValue());
//            case ENTER_USER_IDENTIFIER -> createUserIdentifierPayload(flowToken, twitterAccount.getUsername(), subtask.getValue());
//            case ENTER_PASSWORD -> createPasswordPayload(flowToken, twitterAccount.getPassword(), subtask.getValue());
//            default -> createAccDuplicationCheckrPayload(flowToken, subtask.getValue());
//        };
//    }

    private LoginSubtaskPayload getSubtaskPayload(
            ELoginSubtasks subtask,
            String flowToken,
            TwitterAccount twitterAccount
    ) {
        return switch (subtask) {
            case ENTER_USER_IDENTIFIER -> new LoginSubtaskPayload(flowToken, subtask, twitterAccount.getUsername());
            case ENTER_PASSWORD -> new LoginSubtaskPayload(flowToken, subtask, twitterAccount.getPassword());
            default -> new LoginSubtaskPayload(flowToken, subtask);
        };
    }

    @Override
    public void login(TwitterAccount twitterAccount) {
        twitterAccountRepo.updateCsrfToken(twitterAccount.getId(), null);
        try {
            for (int i = 0; i < 5; i++) {
                getUserCredential(twitterAccount);
                TwitterAccount account = twitterAccountRepo.findById(twitterAccount.getId()).orElseThrow(() -> new RuntimeException("No acc"));
                if (nonNull(account.getCsrfToken())) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error("Error during logging in " + e + " : " + twitterAccount.getUsername());
            twitterAccountRepo.updateStatus(twitterAccount.getId(), TwitterAccountStatus.INVALID_COOKIES);
        }
    }

    @Override
    public void unlock(TwitterAccount twitterAccount) {
        try {
            String html = apiRequests.getAccessPage(twitterAccount);
            XCaptchaToken tokens = extractTokensFromAccessHtmlPage(html);
            String jsInst = apiRequests.getJsInst(twitterAccount);
            String resp = apiRequests.postToAccessPage(twitterAccount, tokens, jsInst);
            tokens = extractTokensFromAccessHtmlPage(resp);
            String capsolverToken = captchaResolver.solveCaptcha();
            String tokenResp = apiRequests.postToAccessPageWithToken(twitterAccount, tokens, capsolverToken);
            tokens = extractTokensFromAccessHtmlPage(tokenResp);
            capsolverToken = captchaResolver.solveCaptcha();
            tokenResp = apiRequests.postToAccessPageWithToken(twitterAccount, tokens, capsolverToken);
            tokens = extractTokensFromAccessHtmlPage(tokenResp);
            jsInst = apiRequests.getJsInst(twitterAccount);
            resp = apiRequests.postToAccessPage(twitterAccount, tokens, jsInst);
        } catch (Exception e) {
            log.error("UNLOCK EX : " + e);
        }
    }

    private void getUserCredential(TwitterAccount twitterAccount) throws IOException {
        if (nonNull(twitterAccount.getProxy())) {
            AccountCredential accCred = toAccountCredential(twitterAccount);
            this.cred = getGuestCredential(twitterAccount.getProxy());
            initiateLogin(twitterAccount.getProxy());

            for (int i = 0; i < subtasks.length; i++) {
                LoginSubtaskPayload payload = getSubtaskPayload(subtasks[i], flowToken, twitterAccount);
                OkHttpClient client = okHttp3ClientService.createClientWithProxy(twitterAccount.getProxy());
                Response response = executeLoginSubtask(payload, client);
                String jsonResponse = response.body().string();
                handleLoginSubtaskResponse(response, i, twitterAccount, jsonResponse);
            }
            twitterAccountRepo.updateCsrfToken(twitterAccount.getId(), this.cred.getCsrfToken());
        } else {
            throw new RuntimeException("Proxy must not be empty");
        }

    }

    private void initiateLogin(Proxy proxy) throws IOException {
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

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
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

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
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

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
//        ObjectMapper mapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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

    private XCaptchaToken extractTokensFromAccessHtmlPage(String html) {
        Pattern pattern = Pattern.compile("name=\"authenticity_token\" value=\"([^\"]+)\"|name=\"assignment_token\" value=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            String authenticityToken = Objects.requireNonNull(matcher.group(0));
            log.info("authenticityToken : " + authenticityToken);
            String assignmentToken = Objects.requireNonNull(matcher.group(1));
            log.info("assignmentToken : " + assignmentToken);
            return XCaptchaToken.builder()
                    .assignmentToken(assignmentToken)
                    .authenticityToken(authenticityToken)
                    .build();
        } else {
            throw new RuntimeException("Tokens from html page are not extracted");
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

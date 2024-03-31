package com.twittersfs.server.services.twitter.auth;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twittersfs.server.captcha.CaptchaResolver;
import com.twittersfs.server.dtos.twitter.auth.common.AuthCredential;
import com.twittersfs.server.dtos.twitter.auth.request.LoginSubtaskPayload;
import com.twittersfs.server.dtos.twitter.auth.response.FlowToken;
import com.twittersfs.server.dtos.twitter.auth.response.Subtask;
import com.twittersfs.server.dtos.twitter.auth.response.SubtaskId;
import com.twittersfs.server.dtos.twitter.auth.unclock.XAccessPageResp;
import com.twittersfs.server.dtos.twitter.auth.unclock.XCaptchaToken;
import com.twittersfs.server.entities.Proxy;
import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.enums.ELoginSubtasks;
import com.twittersfs.server.enums.ELoginUrls;
import com.twittersfs.server.enums.TwitterAccountStatus;
import com.twittersfs.server.okhttp3.OkHttp3ClientService;
import com.twittersfs.server.repos.TwitterAccountRepo;
import com.twittersfs.server.services.twitter.readonly.TwitterApiRequests;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ConnectException;
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
                ELoginSubtasks.ENTER_PASSWORD,
                ELoginSubtasks.ACCOUNT_DUPLICATION_CHECK
        };
    }

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
        OkHttpClient client = okHttp3ClientService.createClientWithProxy(twitterAccount.getProxy());
        try {
            this.cred = new AuthCredential();
            getUserCredential(twitterAccount, client);
        } catch (ConnectException e) {
            log.error("Proxy error during logging in " + e + " : " + twitterAccount.getUsername());
            twitterAccountRepo.updateStatus(twitterAccount.getId(), TwitterAccountStatus.PROXY_ERROR);
        } catch (Exception e) {
            log.error("Error during logging in " + e + " : " + twitterAccount.getUsername());
            twitterAccountRepo.updateStatus(twitterAccount.getId(), TwitterAccountStatus.INVALID_COOKIES);
        }
    }

    @Override
    public void unlock(TwitterAccount twitterAccount) {
        try {
            XAccessPageResp accessPage = apiRequests.getAccessPage(twitterAccount);
            String cookies = accessPage.getCookies();
            XCaptchaToken tokens = extractTokensFromAccessHtmlPage(accessPage.getHtml());
            String jsInst = apiRequests.getJsInst(twitterAccount);
            String resp = apiRequests.postToAccessPage(twitterAccount, tokens, jsInst, cookies);
            tokens = extractTokensFromAccessHtmlPage(resp);
            String capsolverToken;
            String tokenResp;
            for (int i = 0; i < 2; i++) {
                try {
                    capsolverToken = captchaResolver.solveCaptcha();
                    tokenResp = apiRequests.postToAccessPageWithToken(twitterAccount, tokens, capsolverToken, cookies);
                    tokens = extractTokensFromAccessHtmlPage(tokenResp);
                } catch (Exception e) {
//                    log.error("Getting captcha token error : " + e + " account : " + twitterAccount.getUsername());
                }
            }
            jsInst = apiRequests.getJsInst(twitterAccount);
            resp = apiRequests.postToAccessPage(twitterAccount, tokens, jsInst, cookies);
        } catch (Exception e) {
            log.error("Solving captcha error : " + e);
        }
    }

    private void getUserCredential(TwitterAccount twitterAccount, OkHttpClient client) throws IOException {
        if (nonNull(twitterAccount.getProxy())) {
            this.cred = getGuestCredential(twitterAccount.getProxy());
            initiateLogin(twitterAccount.getProxy(), client);
            for (int i = 0; i < subtasks.length; i++) {
                LoginSubtaskPayload payload = getSubtaskPayload(subtasks[i], flowToken, twitterAccount);
//                OkHttpClient client = okHttp3ClientService.createClientWithProxy(twitterAccount.getProxy());
                Response response = executeLoginSubtask(payload, client);
                handleLoginSubtaskResponse(response, i, twitterAccount);
            }
            twitterAccountRepo.updateCsrfToken(twitterAccount.getId(), this.cred.getCsrfToken());
        } else {
            throw new RuntimeException("Proxy must not be empty");
        }

    }

    private void initiateLogin(Proxy proxy, OkHttpClient client) throws IOException {
//        OkHttpClient client = okHttp3ClientService.createClientWithProxy(proxy);
//        log.info("INIT");
        Request request = new Request.Builder()
                .url(ELoginUrls.INITIATE_LOGIN.getValue())
                .post(RequestBody.create(null, new byte[0]))
                .headers(this.cred.toHeader().getHeaders())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response);
            }
            String jsonResponse = response.body().string();
            this.flowToken = mapper.readValue(jsonResponse, FlowToken.class).getFlowToken();
//            log.info("INIT Log : " + jsonResponse);
            String[] setCookieHeaders = response.headers("Set-Cookie").toArray(new String[0]);
            String cookies = String.join(";", setCookieHeaders);
            this.cred.setCookies(cookies);
        }
    }

    private AuthCredential getGuestCredential(Proxy proxy) throws IOException {
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
//            log.info("GC : " + jsonResponse);
            JsonNode jsonNode = mapper.readTree(jsonResponse);
            String guestToken = jsonNode.get("guest_token").asText();
            cred.setGuestToken(guestToken);
        }

        return cred;
    }

    private Response executeLoginSubtask(LoginSubtaskPayload payload, OkHttpClient client) throws IOException {
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json"), mapper.writeValueAsString(payload));

        Request request = new Request.Builder()
                .url(ELoginUrls.LOGIN_SUBTASK.getValue())
                .post(requestBody)
                .headers(this.cred.toHeader().getHeaders())
                .build();

        return client.newCall(request).execute();
    }

    private void handleLoginSubtaskResponse(Response response, int i, TwitterAccount account) throws IOException {
        String[] setCookieHeaders = response.headers("Set-Cookie").toArray(new String[0]);
        String jsonResponse = response.body().string();
        String cookies = String.join(";", setCookieHeaders);
//        log.info("ITER : " + i + " Cookies : " + cookies);
//        log.info("ITER : " + i + " RESP : " + jsonResponse);
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
        String authenticityToken = "";
        String assignmentToken = "";
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                authenticityToken = matcher.group(1);
//                log.info("AU : " + authenticityToken);
            }
            if (matcher.group(2) != null) {
                assignmentToken = matcher.group(2);
//                log.info("AS : " + assignmentToken);
            }
        }
        return XCaptchaToken.builder()
                .assignmentToken(assignmentToken)
                .authenticityToken(authenticityToken)
                .build();
    }
}

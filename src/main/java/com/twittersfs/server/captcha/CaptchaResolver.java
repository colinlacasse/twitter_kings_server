package com.twittersfs.server.captcha;

import com.twittersfs.server.captcha.CapsolverClient;
import com.twittersfs.server.captcha.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CaptchaResolver {
    @Value("${capsolver.api.key}")
    private String apiKey;
    @Value("${capsolver.api.task}")
    private String apiTask;
    @Value("${capsolver.website.key}")
    private String websiteKey;
    @Value("${capsolver.website.url}")
    private String websiteUrl;
    private final CapsolverClient capsolverClient;

    public CaptchaResolver(CapsolverClient capsolverClient) {
        this.capsolverClient = capsolverClient;
    }

    public String solveCaptcha() throws InterruptedException{
        TaskResponse response = runTask();
        int counter = 0;
        while (counter < 3) {
            TokenResponse tokenResponse = receiveToken(response.getTaskId());
            if (tokenResponse != null && tokenResponse.getStatus().equals("ready")) {
                log.info(tokenResponse.getSolution().getToken());
                return tokenResponse.getSolution().getToken();
            }
            counter++;
            Thread.sleep(5000);
        }
        throw new RuntimeException("Can't receive captcha token response");
    }

    private TaskResponse runTask() {
        TaskRequest request = new TaskRequest();
        request.setClientKey(apiKey);
        Task task = new Task();
        task.setType(apiTask);
        task.setWebsiteURL(websiteUrl);
        task.setWebsitePublicKey(websiteKey);
        request.setTask(task);
        return capsolverClient.createTask(request);
    }

    private TokenResponse receiveToken(String taskId) {
        TokenRequest request = new TokenRequest();
        request.setClientKey(apiKey);
        request.setTaskId(taskId);
        return capsolverClient.getToken(request);
    }
}

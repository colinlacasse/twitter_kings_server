package com.twittersfs.server.captcha;

import com.twittersfs.server.captcha.dto.TaskRequest;
import com.twittersfs.server.captcha.dto.TaskResponse;
import com.twittersfs.server.captcha.dto.TokenRequest;
import com.twittersfs.server.captcha.dto.TokenResponse;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
@FeignClient(name = "capsolverClient", url = "https://api.capsolver.com")
public interface CapsolverClient {
    @PostMapping("/createTask")
    @Headers("Content-Type: application/json")
    TaskResponse createTask(TaskRequest request);

    @PostMapping("/getTaskResult")
    @Headers("Content-Type: application/json")
    TokenResponse getToken(TokenRequest request);
}

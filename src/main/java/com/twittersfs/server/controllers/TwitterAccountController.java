package com.twittersfs.server.controllers;

import com.twittersfs.server.dtos.common.PageableResponse;
import com.twittersfs.server.dtos.twitter.account.TwitterAccountCreate;
import com.twittersfs.server.dtos.twitter.account.TwitterAccountData;
import com.twittersfs.server.dtos.twitter.account.TwitterAccountUpdate;
import com.twittersfs.server.dtos.twitter.message.TwitterChatMessageDto;
import com.twittersfs.server.dtos.user.AccountSubscription;
import com.twittersfs.server.entities.TwitterAccount;
import com.twittersfs.server.enums.TwitterAccountStatus;
import com.twittersfs.server.services.TwitterAccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.UnknownHostException;
import java.util.List;

@RestController
@RequestMapping("api/v1/account")
public class TwitterAccountController {
    private final TwitterAccountService twitterAccountService;

    public TwitterAccountController(TwitterAccountService twitterAccountService) {
        this.twitterAccountService = twitterAccountService;
    }

    @GetMapping("/twitter-accounts")
    public PageableResponse<TwitterAccountData> getFilteredTwitterAccounts(Authentication authentication,
                                                                           @RequestParam TwitterAccountStatus status,
                                                                           @RequestParam int page,
                                                                           @RequestParam int size) {
        return twitterAccountService.getFilteredTwitterAccounts(authentication.getPrincipal().toString(), status, page, size);
    }

    @PostMapping("/{modelId}")
    public void createTwitterAccount(Authentication authentication,
                                     @PathVariable Long modelId,
                                     @Valid @NotNull(message = "Request body must not be null")
                                     @RequestBody TwitterAccountCreate dto) throws UnknownHostException {
        twitterAccountService.createTwitterAccount(authentication.getPrincipal().toString(), modelId, dto);
    }

    @PostMapping("/bulk/{modelId}")
    public void createTwitterAccount(Authentication authentication,
                                     @PathVariable Long modelId,
                                     @Valid @NotNull(message = "Request body must not be null")
                                     @RequestBody List<TwitterAccountCreate> dtos) {
        twitterAccountService.createTwitterAccountBulk(authentication.getPrincipal().toString(), modelId, dtos);
    }

    @PostMapping("/{twitterAccountId}/message")
    public void addTwitterChatMessage(@PathVariable Long twitterAccountId,
                                      @Valid @NotNull(message = "Request body must not be null")
                                      @RequestBody TwitterChatMessageDto dto) {
        twitterAccountService.addChatMessage(twitterAccountId, dto);
    }

    @PatchMapping("/{twitterAccountId}")
    public void updateTwitterAccount(@PathVariable Long twitterAccountId,
                                     @Valid @NotNull(message = "Request body must not be null")
                                     @RequestBody TwitterAccountUpdate dto) throws UnknownHostException {
        twitterAccountService.updateTwitterAccount(twitterAccountId, dto);
    }

    @DeleteMapping("/{twitterAccountId}")
    public void deleteTwitterAccount(@PathVariable Long twitterAccountId) {
        twitterAccountService.deleteTwitterAccount(twitterAccountId);
    }

    @DeleteMapping("/{twitterAccountId}/proxy")
    public void deleteProxyFromTwitterAccount(@PathVariable Long twitterAccountId) {
        twitterAccountService.deleteProxyFromTwitterAccount(twitterAccountId);
    }

    @DeleteMapping("/{messageId}")
    public void deleteMessageFromTwitterAccount(@PathVariable Long messageId) {
        twitterAccountService.deleteChatMessage(messageId);
    }

    @PostMapping("/{twitterAccountId}/subscription")
    public void updateSubscription(@PathVariable Long twitterAccountId, @Valid @NotNull(message = "Request body must not be null") @RequestBody AccountSubscription month) {
        twitterAccountService.updateSubscription(twitterAccountId, month.getMonth());
    }
}

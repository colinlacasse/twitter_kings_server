package com.twittersfs.server.dtos.twitter.account;

import com.twittersfs.server.enums.TwitterAccountStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Builder
public class TwitterAccountData {
    private Long id;
    private String username;
    private String model;
    private TwitterAccountStatus status;
    private String paidTo;
    private String proxy;
    private String email;
}

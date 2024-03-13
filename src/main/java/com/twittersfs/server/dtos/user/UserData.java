package com.twittersfs.server.dtos.user;

import com.twittersfs.server.dtos.model.ModelDto;
import com.twittersfs.server.enums.SubscriptionType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class UserData {
    private Float balance;
    private SubscriptionType subscription;
    private Integer active;
    private Integer cooldown;
    private Integer disabled;
    private Integer locked;
    private Integer invalid;
    private Integer stopping;
    private Integer updated;
    private Integer suspended;
    private Integer error;
    private List<ModelDto> models;
}

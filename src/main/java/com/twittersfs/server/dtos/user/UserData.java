package com.twittersfs.server.dtos.user;

import com.twittersfs.server.dtos.model.ModelDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class UserData {
    private Integer balance;
    private List<ModelDto> models;
}

package com.twittersfs.server.captcha.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Task {
    private String type;
    private String websiteURL;
    private List<String> images;
    private String question;
}

package com.twittersfs.server.captcha.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Solution {
    private List<Integer> objects;
}

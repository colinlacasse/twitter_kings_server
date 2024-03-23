package com.twittersfs.server.dtos.twitter.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserIdentifierInput {
    @JsonProperty("setting_responses")
    private List<SettingResponse> settingResponse;
    @JsonProperty("link")
    private String link;

    public UserIdentifierInput(String userId) {
        this.settingResponse = List.of(new SettingResponse(userId));
        this.link = "next_link";
    }

    public static class SettingResponse {
        @JsonProperty("key")
        String key;
        @JsonProperty("response_data")
        ResponseData responseData;

        public SettingResponse(String responseText) {
            this.key = "user_identifier";
            this.responseData = new ResponseData(responseText);
        }
    }

    static class ResponseData {
        @JsonProperty("text_data")
        TextData textData;

        public ResponseData(String username) {
            this.textData = new TextData(username);
        }
    }

    static class TextData {
        @JsonProperty("result")
        String result;

        public TextData(String text) {
            this.result = text;
        }
    }
}

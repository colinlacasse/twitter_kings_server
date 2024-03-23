package com.twittersfs.server.dtos.twitter.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubtaskInput {
    @JsonProperty("subtask_id")
    private String subtaskId;
    @JsonProperty("settings_list")
    private UserIdentifierInput settingsList;
    @JsonProperty("js_instrumentation")
    public JsInstrumentationInput jsInstrumentation;
    @JsonProperty("enter_password")
    public PasswordInput passwordInput;
    @JsonProperty("check_logged_in_account")
    public AccountDuplicationCheckInput accountDuplicationCheckInput;
}

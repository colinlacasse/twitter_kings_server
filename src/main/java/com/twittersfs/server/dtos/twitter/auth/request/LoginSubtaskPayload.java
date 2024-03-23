package com.twittersfs.server.dtos.twitter.auth.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.twittersfs.server.enums.ELoginSubtasks;
import lombok.Getter;
import lombok.Setter;

import static java.util.Objects.nonNull;

@Getter
@Setter
public class LoginSubtaskPayload {
    @JsonProperty("flow_token")
    private String flowToken;
    @JsonProperty("subtask_inputs")
    private LoginSubtaskInput[] subtaskInputs;

    public LoginSubtaskPayload(String flowToken, ELoginSubtasks subtasksId, String inputText) {
        this.flowToken = flowToken;
        this.subtaskInputs = new LoginSubtaskInput[] {new LoginSubtaskInput(inputText, subtasksId)};
    }

    public LoginSubtaskPayload(String flowToken, ELoginSubtasks subtasksId) {
        this.flowToken = flowToken;
        this.subtaskInputs = new LoginSubtaskInput[] {new LoginSubtaskInput(subtasksId)};
    }

    public static class LoginSubtaskInput {
        @JsonProperty("subtask_id")
        public String subtaskId;
        @JsonProperty("js_instrumentation")
        public JsInstrumentationInput jsInstrumentation;
        @JsonProperty("settings_list")
        public UserIdentifierInput userIdentifier;
//        @JsonProperty("enter_text")
//        public AlternateUserIdentifierInput alternateUserIdentifierInput;
        @JsonProperty("enter_password")
        public PasswordInput passwordInput;
        @JsonProperty("check_logged_in_account")
        public AccountDuplicationCheckInput accountDuplicationCheckInput;

        public LoginSubtaskInput(String inputText, ELoginSubtasks subtasksId) {
            this.subtaskId = subtasksId.getValue();
            if (subtasksId == ELoginSubtasks.JS_INSTRUMENTATION) {
                this.jsInstrumentation = new JsInstrumentationInput();
            } else if (subtasksId == ELoginSubtasks.ENTER_USER_IDENTIFIER && nonNull(inputText)) {
                this.userIdentifier = new UserIdentifierInput(inputText);
            }  else if (subtasksId == ELoginSubtasks.ENTER_PASSWORD && nonNull(inputText)) {
                this.passwordInput = new PasswordInput(inputText);
            }
        }

        public LoginSubtaskInput(ELoginSubtasks subtasksId) {
            this.subtaskId = String.valueOf(subtasksId);
            if (subtasksId == ELoginSubtasks.ACCOUNT_DUPLICATION_CHECK) {
                this.accountDuplicationCheckInput = new AccountDuplicationCheckInput();
            }
        }
    }
}

package com.twittersfs.server.services.twitter.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ELoginSubtasks {
    JS_INSTRUMENTATION("LoginJsInstrumentationSubtask"),
    ENTER_USER_IDENTIFIER("LoginEnterUserIdentifierSSO"),
    ENTER_ALTERNATE_USER_IDENTIFIER("LoginEnterAlternateIdentifierSubtask"),
    ENTER_PASSWORD("LoginEnterPassword"),
    ACCOUNT_DUPLICATION_CHECK("AccountDuplicationCheck");

    private final String value;
}

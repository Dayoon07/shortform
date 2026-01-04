package com.e.shortform.domain.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SocialProviderStatus {
    LOCAL("LOCAL"),
    GOOGLE("GOOGLE");

    private final String value;

}

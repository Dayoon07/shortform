package com.e.shortform.domain.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SocialProviderStatus {
    LOCAL("local"),
    GOOGLE("google");

    private final String value;

}

package com.e.shortform.domain.report.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusType {

    PENDING("PENDING"),
    REVIEWING("REVIEWING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    RESOLVED("RESOLVED");

    private final String value;

}

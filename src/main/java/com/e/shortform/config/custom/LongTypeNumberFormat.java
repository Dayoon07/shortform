package com.e.shortform.config.custom;

public class LongTypeNumberFormat {

    public String formatViewCount(Long value) {
        if (value >= 1_000_000_000L) { // 10억 이상
            return String.format("%.1fB", value / 1_000_000_000.0);
        } else if (value >= 1_000_000L) { // 백만 이상
            return String.format("%.1fM", value / 1_000_000.0);
        } else if (value >= 1_000L) { // 천 이상
            return String.format("%.1fK", value / 1_000.0);
        } else {
            return value.toString();
        }
    }

}

package com.e.shortform.config.custom;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimestampDateFormat {

    public String format(LocalDateTime time) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(time, now);
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "방금 전";
        } else if (seconds < 3600) {
            return (seconds / 60) + "분 전";
        } else if (seconds < 86400) {
            return (seconds / 3600) + "시간 전";
        } else if (seconds < 604800) {
            return (seconds / 86400) + "일 전";
        } else if (seconds < 2592000) { // 약 30일
            return (seconds / 604800) + "주 전";
        } else if (seconds < 31536000) { // 약 1년
            return (seconds / 2592000) + "개월 전";
        } else {
            return (seconds / 31536000) + "년 전";
        }
    }

}

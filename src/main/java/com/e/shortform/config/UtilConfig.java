package com.e.shortform.config;

import com.e.shortform.common.LongTypeNumberFormat;
import com.e.shortform.common.TimestampDateFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilConfig {

    @Bean
    public LongTypeNumberFormat longTypeNumberFormat() {
        return new LongTypeNumberFormat();
    }

    @Bean
    public TimestampDateFormat timestampDateFormat() {
        return new TimestampDateFormat();
    }

}

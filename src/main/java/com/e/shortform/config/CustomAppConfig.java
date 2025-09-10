package com.e.shortform.config;

import com.e.shortform.config.custom.LongTypeNumberFormat;
import com.e.shortform.config.custom.TimestampDateFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomAppConfig {

    @Bean
    public LongTypeNumberFormat longTypeNumberFormat() {
        return new LongTypeNumberFormat();
    }

    @Bean
    public TimestampDateFormat timestampDateFormat() {
        return new TimestampDateFormat();
    }

}

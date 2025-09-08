package com.e.shortform.config;

import com.e.shortform.config.custom.LongTypeNumberFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomAppConfig {

    @Bean
    public LongTypeNumberFormat longTypeNumberFormat() {
        return new LongTypeNumberFormat();
    }

}

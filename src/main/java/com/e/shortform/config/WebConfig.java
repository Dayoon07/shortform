package com.e.shortform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String userHome = System.getProperty("user.home").replace("\\", "/") + "/Desktop/shortform-server";

        String imagePath = "file:///" + userHome + "/shortform-user-profile-img/";
        String videoPath = "file:///" + userHome + "/shortform-user-video/";

        registry.addResourceHandler("/resources/shortform-user-profile-img/**")
                .addResourceLocations(imagePath);

        registry.addResourceHandler("/resources/shortform-user-video/**")
                .addResourceLocations(videoPath);
    }
}

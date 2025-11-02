package com.e.shortform.config;

import com.e.shortform.common.interceptor.CheckSessionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    public CheckSessionInterceptor checkSessionInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String userHome = System.getProperty("user.home").replace("\\", "/") + "/Desktop/shortform-server";
        String imagePath = "file:///" + userHome + "/shortform-user-profile-img/";
        String videoPath = "file:///" + userHome + "/shortform-user-video/";
        String communityPostImgPath = "file:///" + userHome + "/shortform-community-post-img/";

        registry.addResourceHandler("/resources/shortform-user-profile-img/**")
            .addResourceLocations(imagePath);
        registry.addResourceHandler("/resources/shortform-user-video/**")
            .addResourceLocations(videoPath);
        registry.addResourceHandler("/resources/shortform-community-post-img/**")
            .addResourceLocations(communityPostImgPath);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(checkSessionInterceptor)
            .addPathPatterns("/api/**")  // API 경로만 적용
            .excludePathPatterns(
                    "/api/user/login",
                    "/api/user/signup"
            );
    }

}

package com.e.shortform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${jwt.secret:mySecretKey}")
    private String JWT_SECRET_VALUE;
    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI openAPI() {
        // 등록된 스키마 이름으로 SecurityRequirement를 생성
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(SECURITY_SCHEME_NAME);

        // Components에 SECURITY_SCHEME_NAME으로 스키마를 정의
        Components components = new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                .name(SECURITY_SCHEME_NAME) // 스키마 이름은 명확한 상수를 사용
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
        );

        // OpenAPI 객체 생성 시 components와 securityItem을 모두 추가
        return new OpenAPI()
                .info(apiInfo())
                .components(components) // 컴포넌트 추가
                .addSecurityItem(securityRequirement); // 보안 요구사항 추가
    }

    private Info apiInfo() {
        return new Info()
                .title("shortform api list")
                .description("api 서버 문서 정리")
                .version("1.0.0");
    }
}
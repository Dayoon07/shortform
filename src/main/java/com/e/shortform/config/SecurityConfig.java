package com.e.shortform.config;

import com.e.shortform.config.oauth.CustomOAuth2UserService;
import com.e.shortform.config.oauth.OAuth2SuccessHandler;
import com.e.shortform.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final OAuth2SuccessHandler successHandler;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil, UserService userService) {
        return new JwtAuthenticationFilter(jwtUtil, userService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
        JwtAuthenticationFilter jwtAuthenticationFilter,
        CustomOAuth2UserService oAuth2UserService,
        OAuth2SuccessHandler oAuth2SuccessHandler)
            throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // SecurityConfig.java의 filterChain 메서드 수정
                .authorizeHttpRequests(auth -> auth
                // 1. 로그인/회원가입은 인증 없이 접근 허용
                .requestMatchers("/api/user/login", "/api/user/signup").permitAll()
                // 2. 정적 리소스 경로 허용 (WebConfig.java를 보면 /resources/** 경로를 사용합니다.)
                .requestMatchers("/resources/**").permitAll()
                // 3. OAuth2 관련 엔드포인트 허용
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                // 4. 나머지 모든 /api/** 요청은 인증 필요
                .requestMatchers("/api/**").authenticated()
                // or .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);
        return http.build();
    }

}

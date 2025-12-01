package com.e.shortform.common.interceptor;

import com.e.shortform.common.annotation.RequireAuth;
import com.e.shortform.config.JwtUtil;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequireAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(
            @NotNull HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull Object handler)
            throws Exception {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireAuth requireAuth = handlerMethod.getMethodAnnotation(RequireAuth.class);

        // @RequireAuth 애노테이션이 없으면 통과
        if (requireAuth == null) {
            return true;
        }

        // JWT 토큰 추출
        String token = getTokenFromRequest(request);

        if (token == null) {
            return sendUnauthorized(response, requireAuth.message());
        }

        if (!jwtUtil.validateToken(token)) {
            return sendUnauthorized(response, "유효하지 않은 토큰입니다.");
        }

        try {
            String username = jwtUtil.getUsernameFromToken(token);
            UserEntity user = userService.findByUsername(username);

            if (user == null) {
                return sendUnauthorized(response, "사용자를 찾을 수 없습니다.");
            }

            // ✅ SecurityContext에 인증 정보 저장
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            return true;

        } catch (Exception e) {
            log.error("JWT 인증 중 오류 발생", e);
            return sendUnauthorized(response, "인증 처리 중 오류가 발생했습니다.");
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private boolean sendUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);

        response.getWriter().write(objectMapper.writeValueAsString(result));
        return false;
    }


}
package com.e.shortform.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.e.shortform.common.annotation.CheckSession;
import java.util.HashMap;
import java.util.Map;

@Component
public class CheckSessionInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        CheckSession checkSession = handlerMethod.getMethodAnnotation(CheckSession.class);

        if (checkSession == null) {
            return true;
        }

        HttpSession session = request.getSession(false);

        // 세션이 없거나 로그인 정보가 없으면 차단
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", checkSession.message());

            response.getWriter().write(objectMapper.writeValueAsString(result));
            return false;
        }

        return true;
    }
}
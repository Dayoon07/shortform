package com.e.shortform.config.oauth;

import com.e.shortform.config.JwtUtil;
import com.e.shortform.domain.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.*;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication auth) throws IOException {

        CustomOAuth2User principal = (CustomOAuth2User) auth.getPrincipal();
        UserEntity user = principal.getUser();

        String token = jwtUtil.generateToken(user);

        // 쿠키에 저장 (httpOnly, secure)
        Cookie cookie = new Cookie("accessTkn", token);
        cookie.setHttpOnly(false);
        cookie.setSecure(false); // HTTPS 환경에서만
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60); // 24시간
        res.addCookie(cookie);

        System.out.println(getClass().getName() + " - " + cookie + ", " + principal);

        // 프론트엔드로 리다이렉트 (토큰은 쿠키에 있음)
        String redirectUrl = "http://localhost:3000/shortform-client/oauth/callback";
        getRedirectStrategy().sendRedirect(req, res, redirectUrl);
    }

}
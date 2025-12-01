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

        String redirectUrl = "http://localhost:3000/shortform-client/social-login?token=" + token;

        getRedirectStrategy().sendRedirect(req, res, redirectUrl);
    }
}

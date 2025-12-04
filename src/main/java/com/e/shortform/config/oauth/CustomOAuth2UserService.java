package com.e.shortform.config.oauth;

import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) {
        OAuth2User oAuth2User = super.loadUser(req);

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        // OAuth2 제공자 정보 추출 (registrationId)
        String provider = req.getClientRegistration().getRegistrationId(); // "google", "naver", etc.
        System.out.printf("%s", provider);

        UserEntity user = userService.findByMail(email);

        if (user == null) {
            // 새로운 소셜 사용자 생성
            user = userService.createSocialUser(email, name, picture, provider);
        } else {
            // 기존 사용자의 경우, provider 정보 업데이트 (이미 계정이 있는 경우)
            if (!user.isSocial()) {
                // 로컬 계정에 소셜 계정 연동하는 경우
                user.setSocial(true);
                user.setProvider(provider);
                userService.updateUser(user);
            }
        }

        log.info("소셜 계정을 가진 유저가 로그인 했습니다: {}", user);
        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }
}
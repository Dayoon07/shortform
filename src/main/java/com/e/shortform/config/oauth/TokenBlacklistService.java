package com.e.shortform.config.oauth;

import com.e.shortform.config.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    /** 토큰을 블랙리스트에 추가 */
    public void addToBlacklist(String token) {
        try {
            // 토큰의 남은 유효 시간 계산
            Long remainingTime = jwtUtil.getTokenRemainingTime(token);

            if (remainingTime > 0) {
                // Redis에 저장 (만료 시간 설정)
                redisTemplate.opsForValue().set(
                        "shortform-server-blacklist:" + token,
                        "logout",
                        remainingTime,
                        TimeUnit.MILLISECONDS
                );
                log.info("토큰이 블랙리스트에 추가됨: {}", token.substring(0, 20) + "...");
            }
        } catch (Exception e) {
            log.error("블랙리스트 추가 실패", e);
            throw new RuntimeException("로그아웃 처리 중 오류가 발생했습니다");
        }
    }

    /** 토큰이 블랙리스트에 있는지 확인 */
    public boolean isBlacklisted(String token) {
        try {
            return redisTemplate.hasKey("shortform-server-blacklist:" + token);
        } catch (Exception e) {
            log.error("블랙리스트 확인 실패", e);
            return false;
        }
    }

}

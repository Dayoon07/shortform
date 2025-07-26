package com.e.shortform.model.service;

import com.e.shortform.model.entity.FollowEntity;
import com.e.shortform.model.entity.UserEntity;
import com.e.shortform.model.mapper.FollowMapper;
import com.e.shortform.model.repository.FollowRepo;
import com.e.shortform.model.repository.UserRepo;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowMapper followMapper;
    private final FollowRepo followRepo;

    private final UserRepo userRepo;

    public Map<String, Object> follow(String mention, UserEntity user) {
        Map<String, Object> result = new HashMap<>();

        if (user == null) {
            result.put("success", false);
            result.put("message", "로그인 정보가 없습니다.");
            return result;
        }

        System.out.println(user);

        UserEntity targetUser = userRepo.findByMention(mention);
        if (targetUser == null) {
            result.put("success", false);
            result.put("message", "해당 멘션을 가진 사용자가 존재하지 않습니다.");
            return result;
        }

        if (user.getId().equals(targetUser.getId())) {
            result.put("success", false);
            result.put("message", "자기 자신은 팔로우할 수 없습니다.");
            return result;
        }

        FollowEntity followEntity = FollowEntity.builder()
                .followUser(user)
                .followedUser(targetUser)
                .build();
        followRepo.save(followEntity);

        result.put("success", true);
        result.put("message", "팔로우 성공");
        result.put("followedUserMention", mention);
        return result;
    }

}

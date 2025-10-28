package com.e.shortform.domain.user.service;

import com.e.shortform.domain.user.entity.FollowEntity;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.mapper.FollowMapper;
import com.e.shortform.domain.user.repository.FollowRepo;
import com.e.shortform.domain.user.repository.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

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

    /** 팔로우 관계 확인 메서드 */
    public boolean isFollowing(Long followUserId, Long followedUserId) {
        return followRepo.existsByFollowUserIdAndFollowedUserId(followUserId, followedUserId);
    }

    /** 엔티티 객체로 팔로우 관계 확인 */
    public boolean isFollowing(UserEntity followUser, UserEntity followedUser) {
        return followRepo.existsByFollowUserAndFollowedUser(followUser, followedUser);
    }

    /** 상호 팔로우 관계 확인 (맞팔로우) */
    public boolean isMutualFollow(Long userId1, Long userId2) {
        return followRepo.existsByFollowUserIdAndFollowedUserId(userId1, userId2) &&
                followRepo.existsByFollowUserIdAndFollowedUserId(userId2, userId1);
    }

    /** 팔로우 관계 생성 */
    public FollowEntity createFollow(UserEntity followUser, UserEntity followedUser) {
        // 이미 팔로우 관계가 있는지 확인
        if (isFollowing(followUser, followedUser)) {
            throw new IllegalStateException("Already following this user");
        }

        FollowEntity follow = FollowEntity.builder()
                .followUser(followUser)
                .followedUser(followedUser)
                .build();

        return followRepo.save(follow);
    }

    /** 팔로우 관계 해제 */
    public void unfollow(Long followUserId, Long followedUserId) {
        Optional<FollowEntity> followRelation =
                followRepo.findByFollowUserIdAndFollowedUserId(followUserId, followedUserId);

        if (followRelation.isPresent()) {
            followRepo.delete(followRelation.get());
        }
    }

    /** 팔로워 수 조회 */
    public Long getFollowerCount(Long userId) {
        return followRepo.countByFollowedUserId(userId);
    }

    /** 팔로잉 수 조회 */
    public Long getFollowingCount(Long userId) {
        return followRepo.countByFollowUserId(userId);
    }

    // 안전한 팔로우 생성 (중복 방지)
    @Transactional
    public boolean createFollowSafely(UserEntity followUser, UserEntity followedUser) {
        try {
            // 이미 팔로우 관계가 있는지 다시 한번 확인
            if (followRepo.existsByFollowUserIdAndFollowedUserId(
                    followUser.getId(), followedUser.getId())) {
                return false; // 이미 팔로우 중
            }

            FollowEntity follow = FollowEntity.builder()
                    .followUser(followUser)
                    .followedUser(followedUser)
                    .build();

            followRepo.save(follow);
            return true; // 성공적으로 팔로우

        } catch (DataIntegrityViolationException e) {
            // 유니크 제약조건 위반 (동시 요청으로 인한 중복)
            return false; // 이미 팔로우됨으로 간주
        } catch (Exception e) {
            throw new RuntimeException("팔로우 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 안전한 언팔로우 (존재하지 않는 관계 처리)
    @Transactional
    public boolean unfollowSafely(Long followUserId, Long followedUserId) {
        try {
            Optional<FollowEntity> followRelation =
                    followRepo.findByFollowUserIdAndFollowedUserId(followUserId, followedUserId);

            if (followRelation.isPresent()) {
                followRepo.delete(followRelation.get());
                return true; // 성공적으로 언팔로우
            } else {
                return false; // 팔로우 관계가 존재하지 않음
            }

        } catch (Exception e) {
            throw new RuntimeException("언팔로우 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /** <h1>팔로우 상태 토글 (한 번의 트랜잭션으로 처리)</h1> */
    @Transactional
    public FollowToggleResult toggleFollow(UserEntity currentUser, UserEntity targetUser) {
        try {
            boolean isCurrentlyFollowing = followRepo.existsByFollowUserIdAndFollowedUserId(
                    currentUser.getId(), targetUser.getId());

            if (isCurrentlyFollowing) {
                // 언팔로우
                boolean unfollowSuccess = unfollowSafely(currentUser.getId(), targetUser.getId());
                return new FollowToggleResult(false, unfollowSuccess, targetUser.getUsername() + "님을 언팔로우했습니다.");
            } else {
                // 팔로우
                boolean followSuccess = createFollowSafely(currentUser, targetUser);
                if (followSuccess) {
                    return new FollowToggleResult(true, true, targetUser.getUsername() + "님을 팔로우했습니다.");
                } else {
                    // 이미 팔로우된 상태 (동시 요청으로 인한)
                    return new FollowToggleResult(true, true, "이미 팔로우 중입니다.");
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("팔로우 상태 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /** 팔로우 토글 결과를 담는 클래스 */
    public static class FollowToggleResult {
        private boolean isFollowing;
        private boolean success;
        private String message;

        public FollowToggleResult(boolean isFollowing, boolean success, String message) {
            this.isFollowing = isFollowing;
            this.success = success;
            this.message = message;
        }

        // getters
        public boolean isFollowing() { return isFollowing; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    public List<FollowEntity> selectAllFollow() {
        return followRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public boolean existsByFollowUserAndFollowedUser(UserEntity followUser, UserEntity followedUser) {
        return followRepo.existsByFollowUserAndFollowedUser(followUser, followedUser);
    }

}

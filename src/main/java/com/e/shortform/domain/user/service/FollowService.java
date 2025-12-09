package com.e.shortform.domain.user.service;

import com.e.shortform.domain.user.entity.FollowEntity;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.mapper.FollowMapper;
import com.e.shortform.domain.user.repository.FollowRepo;
import com.e.shortform.domain.user.repository.UserRepo;
import com.e.shortform.domain.user.res.FollowToggleDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
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

    /**
     * mention 기반으로 팔로우 상태 확인 (최적화된 단일 쿼리)
     * 사용자가 존재하지 않거나 팔로우하지 않은 경우 모두 false 반환
     * (전에 만든 거는 DB 요청을 3번하는 방식이여서 고침)
     */
    public boolean upgradeVerIsFollowingFunc(String reqMention, String resMention) {
        if (reqMention == null || resMention == null)
            return false;
        if (reqMention.trim().isEmpty() || resMention.trim().isEmpty())
            return false;
        if (reqMention.equals(resMention))  // 자기 자신을 팔로우하는지 확인하는 경우
            return false;

        try {
            Boolean result = followMapper.checkFollowStatusByMention(reqMention, resMention);
            return result != null && result;
        } catch (Exception e) {
            log.error("팔로우 상태 확인 중 오류 발생 - reqMention: {}, resMention: {}", reqMention, resMention, e);
            return false;
        }
    }

    /** ID 기반 팔로우 확인 (기존 메서드 유지) */
    public boolean isFollowing(Long followUserId, Long followedUserId) {
        if (followUserId == null || followedUserId == null)
            return false;

        if (followUserId.equals(followedUserId))
            return false;

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

        followRelation.ifPresent(followRepo::delete);
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

    /** <h3>팔로우 상태 토글 (한 번의 트랜잭션으로 처리)</h3> */
    @Transactional
    public FollowToggleDto toggleFollow(UserEntity currentUser, UserEntity targetUser) {
        try {
            boolean isCurrentlyFollowing = followRepo.existsByFollowUserIdAndFollowedUserId(
                    currentUser.getId(), targetUser.getId());

            if (isCurrentlyFollowing) {
                // 언팔로우
                boolean unfollowSuccess = unfollowSafely(currentUser.getId(), targetUser.getId());
                return new FollowToggleDto(false, unfollowSuccess, targetUser.getUsername() + "님을 언팔로우했습니다.");
            } else {
                // 팔로우
                boolean followSuccess = createFollowSafely(currentUser, targetUser);
                if (followSuccess) {
                    return new FollowToggleDto(true, true, targetUser.getUsername() + "님을 팔로우했습니다.");
                } else {
                    // 이미 팔로우된 상태 (동시 요청으로 인한)
                    return new FollowToggleDto(true, true, "이미 팔로우 중입니다.");
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("팔로우 상태 변경 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public List<FollowEntity> selectAllFollow() {
        return followRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public boolean existsByFollowUserAndFollowedUser(UserEntity followUser, UserEntity followedUser) {
        return followRepo.existsByFollowUserAndFollowedUser(followUser, followedUser);
    }

    public Long followExists(Long followUserId, Long followedUserId) {
        return followMapper.followExists(followUserId, followedUserId);
    }

}

package com.e.shortform.domain.follow.repository;

import com.e.shortform.domain.follow.entity.FollowEntity;
import com.e.shortform.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FollowRepo extends JpaRepository<FollowEntity, Long> {

    /** 특정 사용자가 다른 사용자를 팔로우하고 있는지 확인 */
    boolean existsByFollowUserAndFollowedUser(UserEntity followUser, UserEntity followedUser);

    /** 특정 사용자가 다른 사용자를 팔로우하고 있는지 확인 (ID로) */
    boolean existsByFollowUserIdAndFollowedUserId(Long followUserId, Long followedUserId);

    /** 팔로우 관계 엔티티 조회 */
    Optional<FollowEntity> findByFollowUserAndFollowedUser(UserEntity followUser, UserEntity followedUser);

    /** 팔로우 관계 엔티티 조회 (ID로) */
    Optional<FollowEntity> findByFollowUserIdAndFollowedUserId(Long followUserId, Long followedUserId);

    Long countByFollowedUserId(Long followedUserId);

    Long countByFollowUserId(Long followUserId);

}

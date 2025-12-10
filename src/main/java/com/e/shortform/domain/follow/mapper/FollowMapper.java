package com.e.shortform.domain.follow.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FollowMapper {

    /**
     * mention 기반으로 팔로우 상태를 한 번의 쿼리로 확인
     * @param reqMention 팔로우를 확인하는 사용자의 mention
     * @param resMention 팔로우 대상 사용자의 mention
     * @return 팔로우 여부 (사용자가 존재하지 않으면 false)
     */
    Boolean checkFollowStatusByMention(
            @Param("reqMention") String reqMention,
            @Param("resMention") String resMention);

    Long followExists(Long followUserId, Long followedUserId);

}

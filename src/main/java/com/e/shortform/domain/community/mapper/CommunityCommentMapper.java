package com.e.shortform.domain.community.mapper;

import com.e.shortform.domain.community.res.CommunityCommentWithUserDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommunityCommentMapper {

    // 특정 커뮤니티 게시글의 댓글 목록 조회 (답글 개수 포함)
    List<CommunityCommentWithUserDto> selectByCommunityId(Long communityId);

    // 최신순 정렬
    List<CommunityCommentWithUserDto> selectByCommunityIdOrderByDesc(Long communityId);

}

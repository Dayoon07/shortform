package com.e.shortform.domain.community.service;

import com.e.shortform.domain.community.entity.CommunityCommentEntity;
import com.e.shortform.domain.community.entity.CommunityEntity;
import com.e.shortform.domain.community.mapper.CommunityCommentMapper;
import com.e.shortform.domain.community.repository.CommunityCommentRepo;
import com.e.shortform.domain.community.repository.CommunityRepo;
import com.e.shortform.domain.community.res.CommunityCommentWithUserDto;
import com.e.shortform.domain.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommunityCommentService {

    private final CommunityRepo communityRepo;
    private final CommunityCommentRepo communityCommentRepo;

    private final CommunityCommentMapper communityCommentMapper;

    public List<CommunityCommentEntity> findByCommunity(CommunityEntity community) {
        return communityCommentRepo.findByCommunity(community);
    }

    public void insertComment (Long id, String comment, UserEntity user) {
        CommunityEntity b = communityRepo.findById(id).orElseThrow();
        CommunityCommentEntity a = CommunityCommentEntity.builder()
                .commentText(comment)
                .user(user)
                .community(b)
                .deleteStatus(false)
                .build();
        communityCommentRepo.save(a);
    }

    public List<CommunityCommentWithUserDto> findByCommunityIdWithReplyCounts(Long communityId) {
        return communityCommentMapper.selectByCommunityId(communityId);
    }

    public List<CommunityCommentWithUserDto> findByCommunityIdOrderByDesc(Long communityId) {
        return communityCommentMapper.selectByCommunityIdOrderByDesc(communityId);
    }

}

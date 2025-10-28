package com.e.shortform.domain.community.service;

import com.e.shortform.domain.community.entity.CommunityCommentEntity;
import com.e.shortform.domain.community.entity.CommunityEntity;
import com.e.shortform.domain.community.repository.CommunityCommentRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommunityCommentService {

    private final CommunityCommentRepo communityCommentRepo;

    public List<CommunityCommentEntity> findByCommunity(CommunityEntity community) {
        return communityCommentRepo.findByCommunity(community);
    }

}

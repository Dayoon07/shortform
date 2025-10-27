package com.e.shortform.model.service;

import com.e.shortform.model.entity.CommunityCommentEntity;
import com.e.shortform.model.entity.CommunityEntity;
import com.e.shortform.model.repository.CommunityCommentRepo;
import com.e.shortform.model.repository.CommunityRepo;
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

package com.e.shortform.model.service;

import com.e.shortform.model.entity.CommunityAdditionEntity;
import com.e.shortform.model.entity.CommunityEntity;
import com.e.shortform.model.repository.CommunityAdditionRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommunityAdditionService {

    private final CommunityAdditionRepo communityAdditionRepo;

    public List<CommunityAdditionEntity> findAll() {
        return communityAdditionRepo.findAll();
    }

    public List<CommunityAdditionEntity> findByCommunity(CommunityEntity community) {
        return communityAdditionRepo.findByCommunity(community);
    }

}

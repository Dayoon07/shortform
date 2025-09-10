package com.e.shortform.model.repository;

import com.e.shortform.model.entity.CommunityAdditionEntity;
import com.e.shortform.model.entity.CommunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityAdditionRepo extends JpaRepository<CommunityAdditionEntity, Long> {

    List<CommunityAdditionEntity> findByCommunity(CommunityEntity community);

}

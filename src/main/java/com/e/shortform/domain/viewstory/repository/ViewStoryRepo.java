package com.e.shortform.domain.viewstory.repository;

import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.viewstory.entity.ViewStoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViewStoryRepo extends JpaRepository<ViewStoryEntity, Long> {
    List<ViewStoryEntity> findByUser(UserEntity user);
}

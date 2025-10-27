package com.e.shortform.model.repository;

import com.e.shortform.model.entity.UserEntity;
import com.e.shortform.model.entity.ViewStoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViewStoryRepo extends JpaRepository<ViewStoryEntity, Long> {
    List<ViewStoryEntity> findByUser(UserEntity user);
}

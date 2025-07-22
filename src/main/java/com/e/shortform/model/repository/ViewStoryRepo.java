package com.e.shortform.model.repository;

import com.e.shortform.model.entity.ViewStoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ViewStoryRepo extends JpaRepository<ViewStoryEntity, Long> {
}

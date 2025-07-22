package com.e.shortform.model.repository;

import com.e.shortform.model.entity.SearchListEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchListRepo extends JpaRepository<SearchListEntity, Long> {
}

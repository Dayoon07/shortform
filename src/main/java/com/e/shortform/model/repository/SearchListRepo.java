package com.e.shortform.model.repository;

import com.e.shortform.model.entity.SearchListEntity;
import com.e.shortform.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchListRepo extends JpaRepository<SearchListEntity, Long> {

    List<SearchListEntity> findByUserAndSearchedWord(UserEntity user, String searchedWord);
}

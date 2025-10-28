package com.e.shortform.domain.search.repository;

import com.e.shortform.domain.search.entity.SearchListEntity;
import com.e.shortform.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchListRepo extends JpaRepository<SearchListEntity, Long> {

    List<SearchListEntity> findByUserAndSearchedWord(UserEntity user, String searchedWord);
}

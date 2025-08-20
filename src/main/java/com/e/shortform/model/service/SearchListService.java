package com.e.shortform.model.service;

import com.e.shortform.model.entity.SearchListEntity;
import com.e.shortform.model.entity.UserEntity;
import com.e.shortform.model.mapper.SearchListMapper;
import com.e.shortform.model.repository.SearchListRepo;
import com.e.shortform.model.repository.UserRepo;
import com.e.shortform.model.vo.SearchListVo;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchListService {

    private final SearchListRepo searchListRepo;
    private final SearchListMapper searchListMapper;

    public void searchWordRecord(String q) {
        SearchListEntity et = SearchListEntity.builder()
                .searchedWord(q)
                .build();
        searchListRepo.save(et);
    }

    public void searchWordRecord(String q, HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");

        SearchListEntity et = SearchListEntity.builder()
                .user(user)
                .searchedWord(q)
                .build();
        searchListRepo.save(et);
    }

    public List<SearchListVo> selectMySearchList(Long id) {
        return searchListMapper.selectMySearchList(id);
    }


}

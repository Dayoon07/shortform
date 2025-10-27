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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchListService {

    private final SearchListRepo searchListRepo;
    private final SearchListMapper searchListMapper;
    private final UserRepo userRepo;

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

    public List<SearchListEntity> selectAllSearchList() {
        return searchListRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public String deleteSearchWord(Long searchedUserId, String searchedWord) {
        try {
            // 직접 삭제 - 조회 불필요
            int deletedCount = searchListMapper.deleteSearchWord(searchedUserId, searchedWord);
            return deletedCount > 0 ? "검색어 삭제됨" : "삭제할 검색어가 없습니다";
        } catch (Exception e) {
            log.error("검색어 삭제 중 오류 발생: {}", e.getMessage());
            return "삭제 실패";
        }
    }

    public List<SearchListEntity> getAllSearchList() {
        return searchListRepo.findAll();
    }

}

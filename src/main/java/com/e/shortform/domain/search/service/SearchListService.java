package com.e.shortform.domain.search.service;

import com.e.shortform.domain.search.entity.SearchListEntity;
import com.e.shortform.domain.search.mapper.SearchListMapper;
import com.e.shortform.domain.search.repository.SearchListRepo;
import com.e.shortform.domain.search.vo.SearchListVo;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.repository.UserRepo;
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

    public void searchWordRecordPlusMention(String q, String mention) {
        UserEntity user = userRepo.findByMention(mention);
        if (user == null) throw new RuntimeException("해당 멘션의 사용자를 찾을 수 없습니다: " + mention);

        SearchListEntity et = SearchListEntity.builder()
                .user(user)
                .searchedWord(q)
                .build();
        searchListRepo.save(et);
    }

    public void searchWordRecordSessionVer(String q) {
        SearchListEntity et = SearchListEntity.builder()
                .searchedWord(q)
                .build();
        searchListRepo.save(et);
    }

    public void searchWordRecordSessionVer(String q, HttpSession session) {
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

    /** id를 기준으로 내림차순 정렬해서 모든 검색 기록을 가져오는 함수 */
    public List<SearchListEntity> selectAllSearchListOrderByDesc() {
        return searchListRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    /**
     * @param id 검색한 유저의 id
     * @param w 검색어
     */
    public String deleteSearchWord(Long id, String w) {
        try {
            int deletedCount = searchListMapper.deleteSearchWord(id, w);
            return deletedCount > 0 ? "검색어 삭제됨" : "삭제할 검색어가 없습니다";
        } catch (Exception e) {
            log.error("검색어 삭제 중 오류 발생: {}", e.getMessage());
            return "삭제 실패";
        }
    }

    /** 정렬 없이 모든 검색 기록을 가져오는 함수 */
    public List<SearchListEntity> findAllSearchList() {
        return searchListRepo.findAll();
    }

}

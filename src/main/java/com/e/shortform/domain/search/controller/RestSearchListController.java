package com.e.shortform.domain.search.controller;

import com.e.shortform.common.annotation.RequireAuth;
import com.e.shortform.domain.comment.service.CommentLikeService;
import com.e.shortform.domain.comment.service.CommentReplyService;
import com.e.shortform.domain.comment.service.CommentService;
import com.e.shortform.domain.community.service.CommunityAdditionService;
import com.e.shortform.domain.community.service.CommunityLikeService;
import com.e.shortform.domain.community.service.CommunityService;
import com.e.shortform.domain.report.service.ReportService;
import com.e.shortform.domain.search.entity.SearchListEntity;
import com.e.shortform.domain.search.service.SearchListService;
import com.e.shortform.domain.search.vo.SearchListVo;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.req.AuthUserReqDto;
import com.e.shortform.domain.follow.service.FollowService;
import com.e.shortform.domain.user.service.UserService;
import com.e.shortform.domain.video.service.VideoLikeService;
import com.e.shortform.domain.video.service.VideoService;
import com.e.shortform.domain.viewstory.service.ViewStoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api/search", produces = "application/json;charset=utf-8")
public class RestSearchListController {

    private final UserService userService;
    private final VideoService videoService;
    private final FollowService followService;
    private final CommentService commentService;
    private final VideoLikeService videoLikeService;
    private final SearchListService searchListService;
    private final ViewStoryService viewStoryService;
    private final CommentLikeService commentLikeService;
    private final CommentReplyService commentReplyService;
    private final CommunityService communityService;
    private final CommunityAdditionService communityAdditionService;
    private final CommunityLikeService communityLikeService;
    private final ReportService reportService;

    @GetMapping("/all")
    public List<SearchListEntity> findAllSearchList() {
        return searchListService.findAllSearchList();
    }

    @GetMapping("/list/all/desc")
    public List<SearchListEntity> selectAllSearchListOrderByDesc() {
        return searchListService.selectAllSearchListOrderByDesc();
    }

    @GetMapping("/list")
    public List<SearchListVo> mySearchList(@RequestParam String id) {
        return searchListService.selectMySearchList(Long.parseLong(id));
    }

    @GetMapping
    public ResponseEntity<?> search(@RequestParam String q) {
        // 로그인 여부 상관없이 검색할 수 있게 만들었으나
        // AuthenticationPrincipal, RequireAuth 애노테이션을 사용하면 
        // 비로그인 유저는 요청할 때 에러가 나기 때문에 context holder를 사용함
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 인증되어 있으면서 UserEntuty 타입
        // 인증이 되어 있지 않으면 검색어만 저장
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserEntity user) {
            if (user.getMention() != null && !user.getMention().isEmpty()) {
                searchListService.searchWordRecordPlusMention(q, user.getMention());
            } else {
                searchListService.searchWordRecord(q);
            }
        } else {
            searchListService.searchWordRecord(q);
        }

        return ResponseEntity.ok(videoService.searchLogic(q));
    }

    @RequireAuth
    @PostMapping("/list/delete")
    public String deleteSearchWord(
            @RequestParam String searchWord,
            @AuthenticationPrincipal AuthUserReqDto user
    ) {
        String result = searchListService.deleteSearchWord(user.getId(), searchWord);
        log.info("삭제한 검색어: {}", searchWord);
        return result;
    }

}

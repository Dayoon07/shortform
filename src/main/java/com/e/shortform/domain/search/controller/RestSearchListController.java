package com.e.shortform.domain.search.controller;

import com.e.shortform.common.annotation.RequireAuth;
import com.e.shortform.domain.comment.service.CommentLikeService;
import com.e.shortform.domain.comment.service.CommentReplyService;
import com.e.shortform.domain.comment.service.CommentService;
import com.e.shortform.domain.community.service.CommunityAdditionService;
import com.e.shortform.domain.community.service.CommunityLikeService;
import com.e.shortform.domain.community.service.CommunityService;
import com.e.shortform.domain.search.entity.SearchListEntity;
import com.e.shortform.domain.search.service.SearchListService;
import com.e.shortform.domain.search.vo.SearchListVo;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.service.FollowService;
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
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api", produces = "application/json;charset=utf-8")
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

    @GetMapping("/search/list/all")
    public List<?> selectAllSearchList() {
        return searchListService.selectAllSearchList();
    }

    @RequireAuth
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q) {
        UserEntity user = (UserEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getMention() == null || user.getMention().isEmpty()) {
            searchListService.searchWordRecord(q);
        } else {
            searchListService.searchWordRecordPlusMention(q, user.getMention());
        }

        return ResponseEntity.ok(videoService.searchLogic(q));
    }

    @GetMapping("/user/search/list")
    public List<SearchListVo> mySearchList(@RequestParam String id) {
        return searchListService.selectMySearchList(Long.parseLong(id));
    }

    @GetMapping("/user/search/all")
    public List<SearchListEntity> getAllSearchLists() {
        return searchListService.getAllSearchList();
    }

    @PostMapping("/search/list/delete")
    public String deleteSearchWord(@RequestParam Long id, @RequestParam String searchWord) {
        String result = searchListService.deleteSearchWord(id, searchWord);
        log.info("deleteSearchWord result: {}", result);
        return result;
    }

}

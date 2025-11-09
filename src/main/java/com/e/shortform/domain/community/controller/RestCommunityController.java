package com.e.shortform.domain.community.controller;

import com.e.shortform.domain.comment.service.CommentLikeService;
import com.e.shortform.domain.comment.service.CommentReplyService;
import com.e.shortform.domain.comment.service.CommentService;
import com.e.shortform.domain.community.service.CommunityAdditionService;
import com.e.shortform.domain.community.service.CommunityLikeService;
import com.e.shortform.domain.community.service.CommunityService;
import com.e.shortform.domain.search.service.SearchListService;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.service.FollowService;
import com.e.shortform.domain.user.service.UserService;
import com.e.shortform.domain.video.service.VideoLikeService;
import com.e.shortform.domain.video.service.VideoService;
import com.e.shortform.domain.viewstory.service.ViewStoryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api", produces = "application/json;charset=utf-8")
public class RestCommunityController {

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

    @GetMapping("/find/community/all")
    public List<?> selectAllCommunity() {
        return communityService.selectAllCommunity();
    }

    @GetMapping("/find/community/addition/all")
    public List<?> selectAllCommunityAddition() {
        return communityAdditionService.findAll();
    }

    @PostMapping("/post/write")
    public ResponseEntity<Map<String, Object>> createPost(
            @RequestParam(value = "content", required = false) String content,
            @RequestParam("visibility") String visibility,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            HttpSession session) {
        return communityService.realCreatePost(content, visibility, images, session);
    }

    @GetMapping("/find/community/list")
    public List<?> selectByCommunityBut(@RequestParam Long id) {
        return communityService.selectByCommunityButWhereId(id);
    }

    @PostMapping("/post/like")
    public ResponseEntity<Map<String, Object>> postLike(@RequestParam String communityUuid, HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("like", "fail", "message", "로그인 필요"));
        }

        Map<String, Object> map = communityLikeService.postLike(communityUuid, session);
        return ResponseEntity.ok(map);
    }

}

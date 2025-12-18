package com.e.shortform.domain.community.controller;

import com.e.shortform.common.annotation.RequireAuth;
import com.e.shortform.domain.comment.service.CommentLikeService;
import com.e.shortform.domain.comment.service.CommentReplyService;
import com.e.shortform.domain.comment.service.CommentService;
import com.e.shortform.domain.community.service.*;
import com.e.shortform.domain.report.service.ReportService;
import com.e.shortform.domain.search.service.SearchListService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api/community", produces = "application/json;charset=utf-8")
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
    private final ReportService reportService;
    private final CommunityCommentService communityCommentService;
    private final CommunityCommentReplyService communityCommentReplyService;

    @GetMapping("/all")
    public List<?> selectAllCommunity() {
        return communityService.selectAllCommunity();
    }

    @GetMapping("/addition/all")
    public List<?> selectAllCommunityAddition() {
        return communityAdditionService.findAll();
    }

    @RequireAuth
    @PostMapping("/write")
    public ResponseEntity<Map<String, Object>> createCommunityPost(
            @RequestParam(value = "content", required = false) String content,
            @RequestParam("visibility") String visibility,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserEntity user) {
        return communityService.realCreatePost(content, visibility, images, user);
    }

    @GetMapping("/find")
    public List<?> selectByCommunityBut(@RequestParam Long writerId) {
        return communityService.selectByCommunityButWhereId(writerId);
    }

    @RequireAuth
    @PostMapping("/like")
    public ResponseEntity<Map<String, Object>> communityLike(
            @RequestParam String communityUuid,
            @AuthenticationPrincipal AuthUserReqDto user
    ) {
        Map<String, Object> map = communityLikeService.postLike(communityUuid, user.getMention());
        return ResponseEntity.ok(map);
    }

    @RequireAuth
    @PostMapping("/delete")
    public ResponseEntity<Boolean> communityDelete(
            @RequestParam Long id) {
        return ResponseEntity.ok(communityService.changeDeleteStatus(id));
    }

    @RequireAuth
    @PostMapping("/comment/insert")
    public ResponseEntity<?> insertComment(
            @RequestParam Long communityId,
            @RequestParam String comment,
            @AuthenticationPrincipal UserEntity user) {
        communityCommentService.insertComment(communityId, comment, user);
        return ResponseEntity.ok(true);
    }

    @RequireAuth
    @PostMapping("/comment/reply/insert")
    public ResponseEntity<?> insertCommentReply(
            @RequestParam String replyText,
            @RequestParam Long commentId,
            @AuthenticationPrincipal UserEntity user
    ) {
        communityCommentReplyService.insertCommentReply(commentId, replyText, user);
        return ResponseEntity.ok(true);
    }


















}

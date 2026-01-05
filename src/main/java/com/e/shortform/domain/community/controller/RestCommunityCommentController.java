package com.e.shortform.domain.community.controller;

import com.e.shortform.common.annotation.RequireAuth;
import com.e.shortform.domain.comment.service.CommentLikeService;
import com.e.shortform.domain.comment.service.CommentReplyService;
import com.e.shortform.domain.comment.service.CommentService;
import com.e.shortform.domain.community.service.*;
import com.e.shortform.domain.follow.service.FollowService;
import com.e.shortform.domain.report.service.ReportService;
import com.e.shortform.domain.search.service.SearchListService;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.service.UserService;
import com.e.shortform.domain.video.service.VideoLikeService;
import com.e.shortform.domain.video.service.VideoService;
import com.e.shortform.domain.viewstory.service.ViewStoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api/community/comment", produces = "application/json;charset=utf-8")
public class RestCommunityCommentController {

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

    @RequireAuth
    @PostMapping("/insert")
    public ResponseEntity<?> insertComment(
            @RequestParam Long communityId,
            @RequestParam String comment,
            @AuthenticationPrincipal UserEntity user) {
        communityCommentService.insertComment(communityId, comment, user);
        return ResponseEntity.ok(true);
    }

    @GetMapping("/list")
    public ResponseEntity<?> getCommunityComments(@RequestParam Long communityId) {
        return ResponseEntity.ok(communityCommentService.findByCommunityIdWithReplyCounts(communityId));
    }

    @GetMapping("/list/recent")
    public ResponseEntity<?> getCommunityCommentsRecent(@RequestParam Long communityId) {
        return ResponseEntity.ok(communityCommentService.findByCommunityIdOrderByDesc(communityId));
    }

}

package com.e.shortform.domain.comment.controller;

import com.e.shortform.common.annotation.RequireAuth;
import com.e.shortform.domain.comment.entity.CommentLikeEntity;
import com.e.shortform.domain.comment.service.CommentLikeService;
import com.e.shortform.domain.comment.service.CommentReplyService;
import com.e.shortform.domain.comment.service.CommentService;
import com.e.shortform.domain.community.service.CommunityAdditionService;
import com.e.shortform.domain.community.service.CommunityLikeService;
import com.e.shortform.domain.community.service.CommunityService;
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

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api/comment/like", produces = "application/json;charset=utf-8")
@RestController
public class RestCommentLikeController {

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
    public List<CommentLikeEntity> getAllComments() {
        return commentLikeService.findAll();
    }

    @RequireAuth
    @PostMapping
    public ResponseEntity<?> commentLikeLogic(
            @RequestParam Long commentId,
            @AuthenticationPrincipal UserEntity user
    ) {
        log.info("댓글 좋아요 요청 {}", commentId);
        boolean isLiked = commentLikeService.toggleCommentLike(commentId, user);
        return ResponseEntity.ok(Map.of("status", isLiked));
    }

}

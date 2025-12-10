package com.e.shortform.domain.comment.controller;

import com.e.shortform.common.annotation.RequireAuth;
import com.e.shortform.domain.comment.entity.CommentReplyEntity;
import com.e.shortform.domain.comment.req.CommentReplyReqDto;
import com.e.shortform.domain.comment.service.CommentLikeService;
import com.e.shortform.domain.comment.service.CommentReplyService;
import com.e.shortform.domain.comment.service.CommentService;
import com.e.shortform.domain.community.service.CommunityAdditionService;
import com.e.shortform.domain.community.service.CommunityLikeService;
import com.e.shortform.domain.community.service.CommunityService;
import com.e.shortform.domain.search.service.SearchListService;
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

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api", produces = "application/json;charset=utf-8")
@RestController
public class RestCommentController {

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

    @RequireAuth
    @PostMapping("/comment/like")
    public ResponseEntity<?> commentLikeLogic(
            @RequestParam Long commentId,
            @AuthenticationPrincipal AuthUserReqDto user
    ) {
        log.info("댓글 좋아요 요청 {}", commentId);
        boolean isLiked = commentLikeService.toggleCommentLike(commentId, user.getMention());
        return ResponseEntity.ok(Map.of("status", isLiked ? "liked" : "unliked"));
    }

    @GetMapping("/comment/all")
    public List<?> selectAllComments() {
        return commentService.selectAllComments();
    }

    @RequireAuth
    @PostMapping("/comment/reply/insert")
    public ResponseEntity<?> commentReplyInsert(
            @RequestBody CommentReplyReqDto req,
            @AuthenticationPrincipal AuthUserReqDto user
    ) throws Exception {
        if (req.getCommentReplyText().trim().isEmpty())
            return ResponseEntity.ok(Map.of("message", "답글에 어떠한 값도 있지 않습니다"));

        log.info("답글 작성 요청 commentReplyId: {}, commentReplyText: {}, reqUserMention: {}",
            req.getCommentReplyId(), req.getCommentReplyText(), user.getMention());

        commentReplyService.commentReplyInsert(req, user);

        return ResponseEntity.ok(Map.of("message", "데이터 보냄"));
    }

    @GetMapping("/comment/reply/all")
    public List<?> selectAllCommentReply() {
        return commentReplyService.selectAllCommentReply();
    }

    @PostMapping("/comment/reply/find/content")
    public ResponseEntity<List<CommentReplyEntity>> findByCommentReply(@RequestParam Long commentId) {
        return ResponseEntity.ok(commentReplyService.findByParentComment(commentId));
    }

}

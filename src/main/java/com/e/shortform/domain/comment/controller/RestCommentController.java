package com.e.shortform.domain.comment.controller;

import com.e.shortform.domain.comment.entity.CommentReplyEntity;
import com.e.shortform.domain.comment.service.CommentLikeService;
import com.e.shortform.domain.comment.service.CommentReplyService;
import com.e.shortform.domain.comment.service.CommentService;
import com.e.shortform.domain.community.service.CommunityAdditionService;
import com.e.shortform.domain.community.service.CommunityLikeService;
import com.e.shortform.domain.community.service.CommunityService;
import com.e.shortform.domain.search.service.SearchListService;
import com.e.shortform.domain.user.service.FollowService;
import com.e.shortform.domain.user.service.UserService;
import com.e.shortform.domain.video.service.VideoLikeService;
import com.e.shortform.domain.video.service.VideoService;
import com.e.shortform.domain.viewstory.service.ViewStoryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api", produces = "application/json;charset=utf-8")
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

    @PostMapping("/comment/like/submit")
    public ResponseEntity<?> commentLikeLogic(@RequestParam Long commentId, HttpSession session) {
        log.info("댓글 좋아요 요청 {}", commentId);
        boolean isLiked = commentLikeService.toggleCommentLike(commentId, session);
        return ResponseEntity.ok(Map.of("status", isLiked ? "liked" : "unliked"));
    }

    @GetMapping("/comment/all")
    public List<?> selectAllComments() {
        return commentService.selectAllComments();
    }

    @PostMapping("/insert/comment/reply")
    public ResponseEntity<?> commentReplyInsertLogicFuncFuck(@RequestBody Map<String, Object> req) {
        log.info("답글 작성 요청 commentReplyId: {}, commentReplyText: {}, commentReplyUserId: {}", req.get("commentReplyId"), req.get("commentReplyText"), req.get("commentReplyUserId"));
        commentReplyService.commentReplyInsertFuck(
                Long.parseLong(req.get("commentReplyId").toString()),
                String.valueOf(req.get("commentReplyText")),
                Long.parseLong(req.get("commentReplyUserId").toString()));
        return ResponseEntity.ok(Map.of("message", "데이터 보냄"));
    }

    @GetMapping("/comment/reply/all")
    public List<?> selectAllCommentReply() {
        return  commentReplyService.selectAllCommentReply();
    }

    @PostMapping("/find/comment/reply/content")
    public ResponseEntity<List<CommentReplyEntity>> findByCommentReply(@RequestParam Long commentId) {
        return ResponseEntity.ok(commentReplyService.findByParentComment(commentId));
    }

}

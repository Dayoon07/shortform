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
import com.e.shortform.domain.report.service.ReportService;
import com.e.shortform.domain.search.service.SearchListService;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.req.AuthUserReqDto;
import com.e.shortform.domain.follow.service.FollowService;
import com.e.shortform.domain.user.service.UserService;
import com.e.shortform.domain.video.res.VideoLikeToggleDto;
import com.e.shortform.domain.video.service.VideoLikeService;
import com.e.shortform.domain.video.service.VideoService;
import com.e.shortform.domain.viewstory.service.ViewStoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api/comment", produces = "application/json;charset=utf-8")
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
    private final ReportService reportService;

    @RequireAuth
    @PostMapping("/insert")
    public ResponseEntity<?> insertComment(
            @RequestParam("commentText") String commentText,
            @RequestParam("commentVideoId") Long commentVideoId,
            @AuthenticationPrincipal AuthUserReqDto user
    ) {
        Map<String, Object> res = commentService.videoInsertComment(commentText, user.getId(), commentVideoId);
        return ResponseEntity.ok(res);
    }

    /** 좋아요 토글 API (MyBatis 사용 - 더 빠른 성능) */
    @RequireAuth
    @PostMapping("/like/by/mention")
    public ResponseEntity<Map<String, Object>> videoLikeToggleByMention(
            @RequestParam Long id,
            @AuthenticationPrincipal UserEntity user
    ) {
        try {
            // MyBatis 방식 사용 (성능상 유리)
            VideoLikeToggleDto result = videoLikeService.toggleLikeWithMyBatis(id, user.getId());

            if (result.isSuccess()) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "isLiked", result.isLiked(),
                        "totalLikes", result.getTotalLikes(),
                        "message", result.getMessage()
                ));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                        "success", false,
                        "message", result.getMessage()
                ));
            }

        } catch (NumberFormatException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "잘못된 비디오 ID입니다"
            ));
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다"
            ));
        }
    }

    @RequireAuth
    @PostMapping("/like")
    public ResponseEntity<?> commentLikeLogic(
            @RequestParam Long commentId,
            @AuthenticationPrincipal AuthUserReqDto user
    ) {
        log.info("댓글 좋아요 요청 {}", commentId);
        boolean isLiked = commentLikeService.toggleCommentLike(commentId, user.getMention());
        return ResponseEntity.ok(Map.of("status", isLiked ? "liked" : "unliked"));
    }

    @GetMapping("/all")
    public List<?> selectAllComments() {
        return commentService.selectAllComments();
    }

    @RequireAuth
    @PostMapping("/reply/insert")
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

    @GetMapping("/reply/all")
    public List<?> selectAllCommentReply() {
        return commentReplyService.selectAllCommentReply();
    }

    @PostMapping("/reply/find/content")
    public ResponseEntity<List<CommentReplyEntity>> findByCommentReply(@RequestParam Long commentId) {
        return ResponseEntity.ok(commentReplyService.findByParentComment(commentId));
    }

}

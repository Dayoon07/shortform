package com.e.shortform.domain.report.controller;

import com.e.shortform.domain.comment.service.CommentLikeService;
import com.e.shortform.domain.comment.service.CommentReplyService;
import com.e.shortform.domain.comment.service.CommentService;
import com.e.shortform.domain.community.service.CommunityAdditionService;
import com.e.shortform.domain.community.service.CommunityLikeService;
import com.e.shortform.domain.community.service.CommunityService;
import com.e.shortform.domain.follow.service.FollowService;
import com.e.shortform.domain.report.req.ReportReqDto;
import com.e.shortform.domain.report.service.ReportService;
import com.e.shortform.domain.search.service.SearchListService;
import com.e.shortform.domain.user.service.UserService;
import com.e.shortform.domain.video.service.VideoLikeService;
import com.e.shortform.domain.video.service.VideoService;
import com.e.shortform.domain.viewstory.service.ViewStoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api/report", produces = "application/json;charset=utf-8")
@RestController
public class RestReportController {

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
    public ResponseEntity<?> getReportAll() {
        return ResponseEntity.ok(reportService.getReportAll());
    }

    @GetMapping("all/jpa")
    public ResponseEntity<?> getReportAllJpaVer() {
        return ResponseEntity.ok(reportService.getReportAllJpaVer());
    }

    @PostMapping
    public String postReport(@RequestBody ReportReqDto req) {
        reportService.saveReport(req);
        return "접수가 완료 되었습니다";
    }

}

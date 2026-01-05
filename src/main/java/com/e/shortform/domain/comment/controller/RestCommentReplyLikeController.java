package com.e.shortform.domain.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api/comment/reply/like", produces = "application/json;charset=utf-8")
@RestController
public class RestCommentReplyLikeController {
}

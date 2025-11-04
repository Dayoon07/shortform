package com.e.shortform.domain.video.controller;

import com.e.shortform.domain.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api", produces = "application/json;charset=utf-8")
public class RestVideoController {

    private final VideoService videoService;

    @GetMapping("/video/like/all")
    public List<?> likeVideoAll() {
        return videoService.findAll();
    }
}

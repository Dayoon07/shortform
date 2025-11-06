package com.e.shortform.domain.user.controller;

import com.e.shortform.domain.community.service.CommunityService;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.service.UserService;
import com.e.shortform.domain.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api", produces = "application/json;charset=utf-8")
public class RestUserController {

    private final UserService userService;
    private final VideoService videoService;
    private final CommunityService communityService;

    @GetMapping("/user/info/{mention}")
    public ResponseEntity<UserEntity> getUserInfo(@PathVariable String mention) {
        return ResponseEntity.ok(userService.findByMention(mention));
    }

    @GetMapping("/user/info/{mention}/video")
    public ResponseEntity<?> getUserVideo(@PathVariable String mention) {
        return ResponseEntity.ok(videoService.selectUserProfilePageAllVideos(mention));
    }

    @GetMapping("/user/post/info")
    public ResponseEntity<?> getUserPosts(@RequestParam String mention) {
        UserEntity user = userService.findByMention(mention);
        return ResponseEntity.ok(communityService.selectByCommunityButWhereIdAsdf(user.getId()));
    }

}

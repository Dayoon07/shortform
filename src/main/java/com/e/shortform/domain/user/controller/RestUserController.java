package com.e.shortform.domain.user.controller;

import com.e.shortform.common.annotation.RequireAuth;
import com.e.shortform.config.JwtUtil;
import com.e.shortform.config.oauth.TokenBlacklistService;
import com.e.shortform.domain.comment.service.CommentLikeService;
import com.e.shortform.domain.comment.service.CommentReplyService;
import com.e.shortform.domain.comment.service.CommentService;
import com.e.shortform.domain.community.service.CommunityAdditionService;
import com.e.shortform.domain.community.service.CommunityLikeService;
import com.e.shortform.domain.community.service.CommunityService;
import com.e.shortform.domain.search.service.SearchListService;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.req.AuthUserReqDto;
import com.e.shortform.domain.user.req.SignupReqDto;
import com.e.shortform.domain.user.res.UserProfileUpdateDto;
import com.e.shortform.domain.follow.service.FollowService;
import com.e.shortform.domain.user.service.UserService;
import com.e.shortform.domain.video.service.VideoLikeService;
import com.e.shortform.domain.video.service.VideoService;
import com.e.shortform.domain.viewstory.service.ViewStoryService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api", produces = "application/json;charset=utf-8")
@RestController
public class RestUserController {

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

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    // 동시 요청 방지를 위한 Map (간단한 해결책)
    private final Map<String, Long> lastRequestTimes = new ConcurrentHashMap<>();

    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User p) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        UserEntity user = (UserEntity) authentication.getPrincipal();

        System.out.println("authentication: " + authentication);
        System.out.println("user: " + user);
        System.out.println("p: " + p);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/user/all")
    public List<UserEntity> selectAll() {
        return userService.selectAllUsers();
    }

    @GetMapping("/user/info/{mention}")
    public ResponseEntity<?> getUserInfo(@PathVariable String mention) {
        UserEntity user = userService.findByMention(mention);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "해당 멘션의 사용자를 찾을 수 없습니다."));
        }
        return ResponseEntity.ok(user);
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

    @GetMapping("/user/profile/info")
    public ResponseEntity<Map<String, Object>> getProfileUserInfo(@RequestParam String mention) {
        UserEntity user = userService.findByMention(mention);
        Map<String, Object> map = new HashMap<>();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "해당 멘션의 사용자를 찾을 수 없습니다."));
        }
        map.put("profileInfo", userService.getUserProfilePageInfo(user.getId()));
        map.put("profileVideosInfo", videoService.selectUserProfilePageAllVideos(mention));
        return ResponseEntity.ok(map);
    }

    @PostMapping("/user/signup")
    public ResponseEntity<String> signup(@RequestBody SignupReqDto req) {
        String t = userService.signup(req.getUsername(), req.getPassword(), req.getEmail(), req.getProfileImage());
        return ResponseEntity.ok(t);
    }

    @PostMapping("/user/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody UserEntity loginRequest,
            HttpSession session,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType) {
        return userService.login(loginRequest.getUsername(), loginRequest.getPassword(), session, clientType);
    }

    @PostMapping("/user/logout")
    public ResponseEntity<Map<String, Object>> logout(
            HttpSession session,
            HttpServletResponse servletRes,
            @RequestHeader("Authorization") String bearerToken) {
        return userService.logout(session, servletRes, bearerToken);
    }

    @GetMapping("/user/chk/username")
    public boolean chkUsername(@RequestParam String username) {
        return userService.selectChkUsername(username);
    }

    @GetMapping("/user/chk/mail")
    public boolean chkUserMail(@RequestParam String mail) {
        return userService.selectChkUserMail(mail);
    }

    @RequireAuth
    @PostMapping("/user/update")
    public ResponseEntity<?> updateUserInfo(
            // 1. JSON 형태의 사용자 정보를 받습니다. (클라이언트에서 "req"라는 키로 전송해야 함)
            @RequestPart(value = "req") UserProfileUpdateDto reqDto,
            // 2. 이미지 파일을 받습니다. (선택적이므로 required = false)
            @RequestPart(value = "profileImg", required = false) MultipartFile profileImg,
            // 3. 기존 이미지 경로를 받습니다. (선택적 String)
            @RequestPart(value = "currentProfileImgSrc", required = false) String currentProfileImgSrc,
            // 4. 인증 정보에서 사용자 ID를 가져옵니다.
            @AuthenticationPrincipal AuthUserReqDto authUser // 이 객체 안에 ID가 있다고 가정
    ) {
        log.info("update req user mention {}", authUser.getMention());
        log.info("update req user info {}", authUser);

        String finalProfileImgPath;
        try {
            // profileImg(새 파일)와 currentProfileImgSrc(기존 경로)를 모두 넘겨서 처리
            // 클라이언트에서 파일 변경 요청이 없으면 finalProfileImgPath는 기존 경로가 되어야 합니다.
            finalProfileImgPath = userService.fileTransfer(profileImg, currentProfileImgSrc);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "500",
                    "message", e.getMessage()
            ));
        }

        userService.updateUserInfo(reqDto, finalProfileImgPath, authUser.getId());

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "프로필이 업데이트되었습니다.",
                "profileImgPath", finalProfileImgPath,
                "user", authUser
        ));
    }


}

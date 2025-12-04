package com.e.shortform.domain.user.controller;

import com.e.shortform.domain.comment.service.CommentLikeService;
import com.e.shortform.domain.comment.service.CommentReplyService;
import com.e.shortform.domain.comment.service.CommentService;
import com.e.shortform.domain.community.service.CommunityAdditionService;
import com.e.shortform.domain.community.service.CommunityLikeService;
import com.e.shortform.domain.community.service.CommunityService;
import com.e.shortform.domain.search.service.SearchListService;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.res.UserProfileUpdateDto;
import com.e.shortform.domain.user.service.FollowService;
import com.e.shortform.domain.user.service.UserService;
import com.e.shortform.domain.video.service.VideoLikeService;
import com.e.shortform.domain.video.service.VideoService;
import com.e.shortform.domain.viewstory.service.ViewStoryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api", produces = "application/json;charset=utf-8")
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

    // 동시 요청 방지를 위한 Map (간단한 해결책)
    private final Map<String, Long> lastRequestTimes = new ConcurrentHashMap<>();

    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserEntity user = (UserEntity) authentication.getPrincipal();
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
    public ResponseEntity<String> signup(
            @RequestParam("email") String email,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("profileImage") MultipartFile profileImage
    ) {
        userService.signup(username, password, email, profileImage);
        return ResponseEntity.ok("회원가입 완료");
    }

    @PostMapping("/user/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody UserEntity loginRequest,
            HttpSession session,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType) {
        return userService.login(loginRequest.getUsername(), loginRequest.getPassword(), session, clientType);
    }

    @PostMapping("/user/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "로그아웃 되었습니다";
    }

    @GetMapping("/user/chk/username")
    public boolean chkUsername(@RequestParam String username) {
        return userService.selectChkUsername(username) == null;
    }

    @GetMapping("/user/chk/mail")
    public boolean chkUserMail(@RequestParam String mail) {
        return userService.selectChkUserMail(mail) == null;
    }

    @PostMapping("/follow")
    public ResponseEntity<Map<String, Object>> follow(@RequestBody Map<String, String> request, HttpSession session) {
        String mention = request.get("mention");
        UserEntity user =  (UserEntity) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.ok(
                    Map.of("message", "세션이 비어있습니다. 로그인 시 팔로우 기능을 사용할 수 있습니다.")
            );
        }

        Map<String, Object> n = followService.follow(mention, user);
        return ResponseEntity.ok(n);
    }

    // 팔로우/언팔로우 토글
    @PostMapping("/follow/toggle")
    public ResponseEntity<Map<String, Object>> toggleFollow(
            @RequestParam String mention,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            UserEntity currentUser = (UserEntity) session.getAttribute("user");
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            // 중복 요청 방지 (1초 내 동일한 요청 차단)
            String requestKey = currentUser.getId() + ":" + mention;
            Long lastRequestTime = lastRequestTimes.get(requestKey);
            long currentTime = System.currentTimeMillis();

            if (lastRequestTime != null && (currentTime - lastRequestTime) < 1000) {
                response.put("success", false);
                response.put("message", "너무 빠른 요청입니다. 잠시 후 다시 시도해주세요.");
                return ResponseEntity.status(429).body(response);
            }

            lastRequestTimes.put(requestKey, currentTime);

            UserEntity targetUser = userService.findByMention(mention);
            if (targetUser == null) {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(response);
            }

            // 자기 자신을 팔로우하려는 경우
            if (currentUser.getId().equals(targetUser.getId())) {
                response.put("success", false);
                response.put("message", "자기 자신을 팔로우할 수 없습니다.");
                return ResponseEntity.status(400).body(response);
            }

            // 팔로우 상태 토글
            FollowService.FollowToggleResult result = followService.toggleFollow(currentUser, targetUser);

            response.put("success", result.isSuccess());
            response.put("isFollowing", result.isFollowing());
            response.put("message", result.getMessage());

            // 업데이트된 팔로워/팔로잉 수 반환
            if (result.isSuccess()) {
                response.put("followerCount", followService.getFollowerCount(targetUser.getId()));
                response.put("followingCount", followService.getFollowingCount(targetUser.getId()));
            }

        } catch (Exception e) {
            e.printStackTrace(); // 로그 확인용
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return ResponseEntity.status(500).body(response);
        } finally {
            // 요청 완료 후 일정 시간 후 캐시에서 제거 (메모리 누수 방지)
            String requestKey = session.getAttribute("user") != null ?
                    ((UserEntity) session.getAttribute("user")).getId() + ":" + mention : "";
            if (!requestKey.isEmpty()) {
                // 5초 후 캐시에서 제거
                CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> {
                    lastRequestTimes.remove(requestKey);
                });
            }
        }

        return ResponseEntity.ok(response);
    }

    // 팔로우 상태 확인
    @GetMapping("/follow/status")
    public ResponseEntity<Map<String, Object>> getFollowStatus(
            @RequestParam String mention,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            UserEntity currentUser = (UserEntity) session.getAttribute("user");
            if (currentUser == null) {
                response.put("isFollowing", false);
                return ResponseEntity.ok(response);
            }

            UserEntity targetUser = userService.findByMention(mention);
            if (targetUser == null) {
                response.put("isFollowing", false);
                return ResponseEntity.ok(response);
            }

            boolean isFollowing = followService.isFollowing(currentUser.getId(), targetUser.getId());
            response.put("isFollowing", isFollowing);

        } catch (Exception e) {
            response.put("isFollowing", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/follow/user/follower/list")
    public ResponseEntity<?> selectProfileUserFollowList(@RequestParam Long id) {
        return ResponseEntity.ok(userService.selectProfileUserFollowList(id));
    }

    @GetMapping("/follow/user/following/list")
    public ResponseEntity<?> selectProfileUserFollowingList(@RequestParam Long id) {
        return ResponseEntity.ok(userService.selectProfileUserFollowingList(id));
    }

    @PostMapping("/user/update")
    public ResponseEntity<?> updateUserInfo(
            @RequestParam String username,
            @RequestParam String mail,
            @RequestParam String mention,
            @RequestParam String bio,
            @RequestParam(value = "profileImg", required = false) MultipartFile profileImg,
            @RequestParam(value = "currentProfileImgSrc", required = false) String currentProfileImgSrc,
            @RequestParam Long id) {

        UserEntity user = userService.findById(id);

        log.info("{}", mention);
        log.info("{}", user);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "status", "fail",
                    "message", "로그인이 필요합니다."
            ));
        }

        String finalProfileImgPath;
        try {
            finalProfileImgPath = userService.fileTransfer(profileImg, currentProfileImgSrc);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }

        UserProfileUpdateDto dto = new UserProfileUpdateDto(
                user.getId(), username, mail, mention, bio, finalProfileImgPath, finalProfileImgPath
        );
        userService.updateUserInfo(dto);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "프로필이 업데이트되었습니다.",
                "profileImgPath", finalProfileImgPath,
                "user", user
        ));
    }

    @GetMapping("/user/follow/all")
    public List<?> selectAllFollow() {
        return followService.selectAllFollow();
    }

    // upgrade는 리액트 화면에서 사용하기 위한 일종의 커스텀 엔드포인트 함수
    // 기존에 있던 follow/toggle의 엔드포인트는 템플릿에서 사용힐 수 있게 남긴 것
    @PostMapping("/follow/toggle/upgrade")
    public ResponseEntity<Map<String, Object>> upgradeToggleFollow(
            @RequestParam String reqMention,
            @RequestParam String resMention) {

        Map<String, Object> response = new HashMap<>();

        UserEntity currentUser = null;
        try {
            currentUser = userService.findByMention(reqMention);
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            // 중복 요청 방지 (1초 내 동일한 요청 차단)
            String requestKey = currentUser.getId() + ":" + resMention;
            Long lastRequestTime = lastRequestTimes.get(requestKey);
            long currentTime = System.currentTimeMillis();

            if (lastRequestTime != null && (currentTime - lastRequestTime) < 1000) {
                response.put("success", false);
                response.put("message", "너무 빠른 요청입니다. 잠시 후 다시 시도해주세요.");
                return ResponseEntity.status(429).body(response);
            }

            lastRequestTimes.put(requestKey, currentTime);

            UserEntity targetUser = userService.findByMention(resMention);
            if (targetUser == null) {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다.");
                return ResponseEntity.status(404).body(response);
            }

            // 자기 자신을 팔로우하려는 경우
            if (currentUser.getId().equals(targetUser.getId())) {
                response.put("success", false);
                response.put("message", "자기 자신을 팔로우할 수 없습니다.");
                return ResponseEntity.status(400).body(response);
            }

            // 팔로우 상태 토글
            FollowService.FollowToggleResult result = followService.toggleFollow(currentUser, targetUser);

            response.put("success", result.isSuccess());
            response.put("isFollowing", result.isFollowing());
            response.put("message", result.getMessage());

            // 업데이트된 팔로워/팔로잉 수 반환
            if (result.isSuccess()) {
                response.put("followerCount", followService.getFollowerCount(targetUser.getId()));
                response.put("followingCount", followService.getFollowingCount(targetUser.getId()));
            }

        } catch (Exception e) {
            e.printStackTrace(); // 로그 확인용
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            return ResponseEntity.status(500).body(response);
        } finally {
            // 요청 완료 후 일정 시간 후 캐시에서 제거 (메모리 누수 방지)
            String requestKey = currentUser != null ?
                    currentUser.getId() + ":" + resMention : "";
            if (!requestKey.isEmpty()) {
                // 5초 후 캐시에서 제거
                CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> {
                    lastRequestTimes.remove(requestKey);
                });
            }
        }

        return ResponseEntity.ok(response);
    }
    // 팔로우 상태 확인
    @GetMapping("/follow/status/upgrade")
    public ResponseEntity<Map<String, Object>> getUpgradeFollowStatus(
            @RequestParam String reqMention,
            @RequestParam String resMention) {

        Map<String, Object> response = new HashMap<>();

        try {
            UserEntity currentUser = userService.findByMention(reqMention);
            if (currentUser == null) {
                response.put("isFollowing", false);
                return ResponseEntity.ok(response);
            }

            UserEntity targetUser = userService.findByMention(resMention);
            if (targetUser == null) {
                response.put("isFollowing", false);
                return ResponseEntity.ok(response);
            }

            boolean isFollowing = followService.isFollowing(currentUser.getId(), targetUser.getId());
            response.put("isFollowing", isFollowing);

        } catch (Exception e) {
            response.put("isFollowing", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }


}

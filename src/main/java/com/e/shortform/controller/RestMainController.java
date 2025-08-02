package com.e.shortform.controller;

import com.e.shortform.model.dto.UserProfileUpdateDto;
import com.e.shortform.model.entity.UserEntity;
import com.e.shortform.model.entity.VideoLikeEntity;
import com.e.shortform.model.service.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class RestMainController {

    private final UserService userService;
    private final VideoService videoService;
    private final FollowService followService;
    private final CommentService commentService;
    private final VideoLikeService videoLikeService;

    static class staticTestObj {
        private String message;

        public staticTestObj(String message) {
            this.message = message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    // 동시 요청 방지를 위한 Map (간단한 해결책)
    private final Map<String, Long> lastRequestTimes = new ConcurrentHashMap<>();

    @GetMapping
    public ResponseEntity<?> hello() {
        staticTestObj m = new staticTestObj("hello");
        return ResponseEntity.ok(m);
    }

    @GetMapping("/user/all")
    public ResponseEntity<?> selectAll() {
        return ResponseEntity.ok(userService.selectAll());
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

    @GetMapping("/user/chk/username")
    public boolean chkUsername(@RequestParam String username) {
        return userService.selectChkUsername(username) == null;
    }

    @GetMapping("/user/chk/mail")
    public boolean chkUserMail(@RequestParam String mail) {
        return userService.selectChkUserMail(mail) == null;
    }

    @PostMapping("/user/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody UserEntity loginRequest,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            UserEntity user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());

            session.setAttribute("user", user); // 또는 최소한의 정보만 저장

            response.put("success", true);
            response.put("message", "로그인 성공");
            response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getMail()
            ));

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (SecurityException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

        } catch (Exception e) {
            log.error("로그인 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "로그인 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/upload/video")
    public ResponseEntity<Map<String, Object>> uploadVidefoComplete(
            @RequestParam("video") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "hashtags", required = false) String hashtags,
            @RequestParam("visibility") String visibility,
            @RequestParam("commentsAllowed") Integer commentsAllowed,
            HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        Map<String, Object> response = videoService.uploadVideo(file, title, description, hashtags, visibility, commentsAllowed, user);
        HttpStatus status = (Boolean) response.get("success") ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(response, status);
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
    @ResponseBody
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
    @ResponseBody
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
            HttpSession session) {

        UserEntity user = (UserEntity) session.getAttribute("user");

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
                "profileImgPath", finalProfileImgPath
        ));
    }

    @PostMapping("/video/insert/comment")
    public ResponseEntity<?> insertComment(
            @RequestParam("commentText") String commentText,
            @RequestParam("commentVideoId") Long commentVideoId,
            HttpSession session
    ) {
        UserEntity user =  (UserEntity) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "사용자가 존재하지 않습니다."
            ));
        }

        Map<String, Object> response = commentService.videoInsertComment(commentText, user.getId(), commentVideoId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/video/find/comment/popular")
    public ResponseEntity<?> findVideoComment(@RequestParam Long id) {
        return ResponseEntity.ok(commentService.selectByCommentId(id));
    }

    @GetMapping("/video/find/comment/recent")
    public ResponseEntity<?> findVideoCommentRecent(@RequestParam Long id) {
        return ResponseEntity.ok(commentService.selectByCommentButOrderByIsDesc(id));
    }

    /**
     * 좋아요 토글 API (MyBatis 사용 - 더 빠른 성능)
     */
    @PostMapping("/video/like")
    public ResponseEntity<?> videoLikeToggle(@RequestBody Map<String, Object> req, HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "세션이 없습니다"
            ));
        }

        try {
            Long videoId = Long.valueOf(req.get("videoId").toString());

            // MyBatis 방식 사용 (성능상 유리)
            VideoLikeService.LikeToggleResult result = videoLikeService.toggleLikeWithMyBatis(videoId, user.getId());

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
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "잘못된 비디오 ID입니다"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다"
            ));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String q) {
        return ResponseEntity.ok(videoService.searchLogic(q));
    }

}

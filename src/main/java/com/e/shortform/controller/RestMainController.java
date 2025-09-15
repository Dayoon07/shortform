package com.e.shortform.controller;

import com.e.shortform.config.JwtUtil;
import com.e.shortform.model.dto.UserProfileUpdateDto;
import com.e.shortform.model.dto.VideoRequestDto;
import com.e.shortform.model.entity.*;
import com.e.shortform.model.service.*;
import com.e.shortform.model.vo.SearchListVo;
import jakarta.servlet.http.HttpSession;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
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
    private final SearchListService searchListService;
    private final ViewStoryService viewStoryService;
    private final CommentLikeService commentLikeService;
    private final CommentReplyService commentReplyService;
    private final CommunityService communityService;
    private final CommunityAdditionService communityAdditionService;
    private final CommunityLikeService communityLikeService;
    private final JwtUtil jwtUtil;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static class staticTestObj {
        private String message;
    }

    // 동시 요청 방지를 위한 Map (간단한 해결책)
    private final Map<String, Long> lastRequestTimes = new ConcurrentHashMap<>();

    @GetMapping
    public ResponseEntity<?> hello() {
        staticTestObj m = new staticTestObj("hello");
        return ResponseEntity.ok(m);
    }

    @GetMapping("/user/all")
    public List<UserEntity> selectAll() {
        return userService.selectAllUsers();
    }

    @GetMapping("/video/all")
    public List<VideoEntity> selectAllVideos() {
        return videoService.selectAllVideos();
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
            HttpSession session,
            @RequestHeader(value = "X-Client-Type", required = false) String clientType) {

        Map<String, Object> response = new HashMap<>();

        try {
            UserEntity user = userService.login(loginRequest.getUsername(), loginRequest.getPassword());

            response.put("success", true);
            response.put("message", "로그인 성공");
            response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "mail", user.getMail(),
                    "profileImgSrc", user.getProfileImgSrc(),
                    "mention", user.getMention(),
                    "createAt", user.getCreateAt()
            ));

            if ("mobile".equals(clientType)) {
                // 모바일/네이티브 앱: JWT 토큰 발행
                String token = jwtUtil.generateToken(user);
                response.put("token", token);
                response.put("tokenType", "Bearer");
            } else {
                // 웹 애플리케이션: 세션 사용
                session.setAttribute("user", user);
            }

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
            @RequestParam("commentsAllowed") String commentsAllowed,
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

        user.setMention(mention);
        session.setAttribute("user", user);

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

    @GetMapping("/user/search/list")
    public List<SearchListVo> mySearchList(@RequestParam String id) {
        return searchListService.selectMySearchList(Long.parseLong(id));
    }

    @PostMapping("/videos/random")
    public ResponseEntity<?> getRandomVideo(@RequestBody VideoRequestDto request, HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");

        try {
            List<Long> excludeIds = request.getExcludeIds();
            if (excludeIds == null) {
                excludeIds = new ArrayList<>();
            } else {
                excludeIds = excludeIds.stream()
                        .filter(Objects::nonNull)
                        .toList();
            }

            log.info("제외할 영상 ID 개수: {}", excludeIds.size());

            Set<Long> excludeIdSet = new HashSet<>(excludeIds);
            VideoEntity randomVideo = videoService.selectRandomVideo(new ArrayList<>(excludeIdSet));

            if (randomVideo == null) {
                return ResponseEntity.ok(Map.of(
                        "message", "더 이상 시청할 영상이 없습니다.",
                        "hasMore", false
                ));
            }

            // 중복 체크 - 혹시 반환된 영상이 제외 목록에 있는지 확인
            if (excludeIdSet.contains(randomVideo.getId())) {
                log.warn("중복 영상이 반환됨: {}", randomVideo.getId());
                for (int i = 0; i < 3; i++) {
                    randomVideo = videoService.selectRandomVideo(new ArrayList<>(excludeIdSet));
                    if (randomVideo != null && !excludeIdSet.contains(randomVideo.getId())) {
                        break;
                    }
                }

                if (randomVideo == null || excludeIdSet.contains(randomVideo.getId())) {
                    return ResponseEntity.ok(Map.of(
                            "message", "더 이상 시청할 영상이 없습니다.",
                            "hasMore", false
                    ));
                }
            }

            Long likeCnt = videoLikeService.countByVideo(randomVideo);
            Long commentCnt = commentService.countByVideo(randomVideo);
            UserEntity uploader = userService.findByMention(randomVideo.getUploader().getMention());
            boolean isLiked = user != null ? videoLikeService.existsByVideoAndUser(randomVideo, user) : false;
            boolean isFollowing = user != null ? followService.existsByFollowUserAndFollowedUser(user, randomVideo.getUploader()) : false;

            log.info("반환된 영상: ID={}, 제목={}", randomVideo.getId(), randomVideo.getVideoTitle());

            if (user != null) {
                viewStoryService.userViewstoryInsert(user.getId(), randomVideo.getId());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("video", randomVideo);
            response.put("uploader", uploader);
            response.put("likeCnt", likeCnt);
            response.put("commentCnt", commentCnt);
            response.put("isLiked", isLiked);
            response.put("isFollowing", isFollowing);
            response.put("hasMore", true);
            response.put("id", randomVideo.getId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("영상 로딩 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "영상을 불러오는 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/user/find/viewstory")
    public List<ViewStoryEntity> viewStoryList(@RequestParam Long id) {
        return viewStoryService.getViewStoryListByUserId(id);
    }

    @PostMapping("/videos/tag")
    public ResponseEntity<List<?>> exploreVideoList(@RequestParam String hashtag) {
        return ResponseEntity.ok(videoService.selectExploreVideoListButTag(hashtag));
    }

    @PostMapping("/comment/like/submit")
    public ResponseEntity<?> commentLikeLogic(@RequestParam Long commentId, HttpSession session) {
        log.info("신호 수신 완료 {}", commentId);
        boolean isLiked = commentLikeService.toggleCommentLike(commentId, session);
        return ResponseEntity.ok(Map.of("status", isLiked ? "liked" : "unliked"));
    }

    @GetMapping("/comment/all")
    public List<?> selectAllComments() {
        return commentService.selectAllComments();
    }

    @PostMapping("/insert/comment/reply")
    public ResponseEntity<?> commentReplyInsertLogicFuncFuck(@RequestBody Map<String, Object> req) {
        log.info("commentReplyId: {}, commentReplyText: {}, commentReplyUserId: {}", req.get("commentReplyId"), req.get("commentReplyText"), req.get("commentReplyUserId"));
        commentReplyService.commentReplyInsertFuck(
                Long.parseLong(req.get("commentReplyId").toString()),
                String.valueOf(req.get("commentReplyText")),
                Long.parseLong(req.get("commentReplyUserId").toString()));
        return ResponseEntity.ok(Map.of("message", "데이터 보냄"));
    }

    @GetMapping("/viewstory/all")
    public List<?> selectAllViewStory() {
        return viewStoryService.selectAllViewStory();
    }

    @GetMapping("/search/list/all")
    public List<?> selectAllSearchList() {
        return searchListService.selectAllSearchList();
    }

    @GetMapping("/user/follow/all")
    public List<?> selectAllFollow() {
        return followService.selectAllFollow();
    }

    @PostMapping("/search/list/delete")
    public String deleteSearchWord(@RequestBody Map<String, Object> req) {
        Object idObj = req.get("id");
        Object wordObj = req.get("searchWord");
        log.info("deleteSearchWord req - id: {}, searchWord: {}", idObj, wordObj);

        Long id = Long.parseLong(String.valueOf(idObj));
        String word = String.valueOf(wordObj).trim();

        String result = searchListService.deleteSearchWord(id, word);

        log.info("deleteSearchWord result: {}", result);
        return result;
    }

    @GetMapping("/comment/reply/all")
    public List<?> selectAllCommentReply() {
        return  commentReplyService.selectAllCommentReply();
    }

    @PostMapping("/find/comment/reply/content")
    public ResponseEntity<List<CommentReplyEntity>> findByCommentReply(@RequestParam Long commentId) {
        return ResponseEntity.ok(commentReplyService.findByParentComment(commentId));
    }

    @PostMapping("/post/write")
    public ResponseEntity<Map<String, Object>> createPost(
            @RequestParam(value = "content", required = false) String content,
            @RequestParam("visibility") String visibility,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 기본 입력 검증
            boolean hasContent = content != null && !content.trim().isEmpty();
            boolean hasImages = images != null && !images.isEmpty() &&
                    images.stream().anyMatch(file -> !file.isEmpty());

            // 내용과 이미지 중 하나라도 있어야 함
            if (!hasContent && !hasImages) {
                response.put("success", false);
                response.put("message", "글 내용 또는 이미지 중 하나는 입력해주세요");
                return ResponseEntity.badRequest().body(response);
            }

            // 내용 길이 검증 (내용이 있을 경우만)
            if (hasContent && content.trim().length() > 2000) {
                response.put("success", false);
                response.put("message", "내용은 2000자 이하로 작성해주세요");
                return ResponseEntity.badRequest().body(response);
            }

            // 이미지 개수 검증
            if (hasImages && images.size() > 5) {
                response.put("success", false);
                response.put("message", "이미지는 최대 5장까지 업로드 가능합니다");
                return ResponseEntity.badRequest().body(response);
            }

            // 파일 크기 및 타입 검증 (이미지가 있을 경우만)
            if (hasImages) {
                long maxSize = 5 * 1024 * 1024; // 5MB
                for (MultipartFile file : images) {
                    if (file.isEmpty()) continue;

                    if (file.getSize() > maxSize) {
                        response.put("success", false);
                        response.put("message", "파일 크기는 5MB 이하여야 합니다");
                        return ResponseEntity.badRequest().body(response);
                    }

                    if (!file.getContentType().startsWith("image/")) {
                        response.put("success", false);
                        response.put("message", "이미지 파일만 업로드 가능합니다");
                        return ResponseEntity.badRequest().body(response);
                    }
                }
            }

            // 서비스 호출 - content가 null이어도 서비스에서 처리
            String result = communityService.createPost(content, visibility, images, session);

            response.put("success", true);
            response.put("message", "게시글이 성공적으로 작성되었습니다");
            response.put("data", result);

            // 로깅도 업데이트
            String postType = getPostTypeForLog(hasContent, hasImages);
            log.info("게시글 작성 성공 - 사용자: {}, 유형: {}, 내용 길이: {}, 이미지 수: {}",
                    session.getAttribute("user"), postType,
                    hasContent ? content.length() : 0,
                    hasImages ? images.size() : 0);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("게시글 작성 실패 - 잘못된 입력: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (SecurityException e) {
            log.warn("게시글 작성 실패 - 권한 없음: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (Exception e) {
            log.error("게시글 작성 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 로깅용 게시글 유형 반환
     */
    private String getPostTypeForLog(boolean hasContent, boolean hasImages) {
        if (hasContent && hasImages) {
            return "텍스트+이미지";
        } else if (hasContent) {
            return "텍스트만";
        } else if (hasImages) {
            return "이미지만";
        } else {
            return "빈 게시글";
        }
    }

    @GetMapping("/find/community/all")
    public List<?> selectAllCommunity() {
        return communityService.selectAllCommunity();
    }

    @GetMapping("/find/community/list")
    public List<?> selectByCommunityBut(@RequestParam Long id) {
        return communityService.selectByCommunityButWhereId(id);
    }

    @GetMapping("/find/community/addition/all")
    public List<?> selectAllCommunityAddition() {
        return communityAdditionService.findAll();
    }

    @PostMapping("/post/like")
    public ResponseEntity<Map<String, Object>> postLike(@RequestParam String communityUuid, HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("like", "fail", "message", "로그인 필요"));
        }

        Map<String, Object> map = communityLikeService.postLike(communityUuid, session);
        return ResponseEntity.ok(map);
    }



}
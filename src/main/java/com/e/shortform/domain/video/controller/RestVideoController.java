package com.e.shortform.domain.video.controller;

import com.e.shortform.domain.comment.service.CommentLikeService;
import com.e.shortform.domain.comment.service.CommentReplyService;
import com.e.shortform.domain.comment.service.CommentService;
import com.e.shortform.domain.community.service.CommunityAdditionService;
import com.e.shortform.domain.community.service.CommunityLikeService;
import com.e.shortform.domain.community.service.CommunityService;
import com.e.shortform.domain.search.service.SearchListService;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.service.FollowService;
import com.e.shortform.domain.user.service.UserService;
import com.e.shortform.domain.video.entity.VideoEntity;
import com.e.shortform.domain.video.req.VideoRequestDto;
import com.e.shortform.domain.video.res.IndexPageAllVideosDto;
import com.e.shortform.domain.video.service.VideoLikeService;
import com.e.shortform.domain.video.service.VideoService;
import com.e.shortform.domain.viewstory.service.ViewStoryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController()
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api", produces = "application/json;charset=utf-8")
public class RestVideoController {

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

    /**
     * 페이징된 비디오 목록 조회 (신규)
     */
    @GetMapping("/video/all")
    public Map<String, Object> selectAllVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size
    ) {
        // 페이지 요청 객체 생성
        PageRequest pageRequest = PageRequest.of(page, size);

        // 페이징된 데이터 조회
        Map<String, Object> result = videoService.selectIndexPageAllVideosPaginated(pageRequest);

        return result;
    }

    /**
     * 전체 비디오 조회 (기존 API 유지 - 하위 호환성)
     */
    @GetMapping("/video/all/legacy")
    public List<IndexPageAllVideosDto> selectAllVideosLegacy() {
        return videoService.selectIndexPageAllVideos();
    }

    @GetMapping("/video/like/all")
    public List<?> likeVideoAll() {
        return videoService.findAll();
    }

    @PostMapping("/upload/video")
    public ResponseEntity<Map<String, Object>> uploadVideoComplete(
            @RequestParam("video") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "hashtags", required = false) String hashtags,
            @RequestParam("visibility") String visibility,
            @RequestParam("commentsAllowed") String commentsAllowed,
            @RequestParam("thumbnail") MultipartFile thumbnail,
            @RequestParam("mention") String mention) {
        UserEntity user = userService.findByMention(mention);
        Map<String, Object> response = videoService.uploadVideo(file, title, description, hashtags, visibility, commentsAllowed, thumbnail, user);
        HttpStatus status = (Boolean) response.get("success") ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(response, status);
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
            boolean isLiked = user != null && videoLikeService.existsByVideoAndUser(randomVideo, user);
            boolean isFollowing = user != null && followService.existsByFollowUserAndFollowedUser(user, randomVideo.getUploader());

            log.info("반환된 영상: ID={}, 제목={}", randomVideo.getId(), randomVideo.getVideoTitle());

            if (user != null) {
                // viewStoryService.userViewstoryInsert(user.getId(), randomVideo.getId());
                videoService.incrementVideoViews(randomVideo.getVideoLoc(), user.getMention());
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

    @PostMapping("/video/swipe/find")
    public ResponseEntity<?> getSwipeVideo(@RequestParam String videoLoc, @RequestParam String mention) {
        UserEntity user = userService.findByMention(mention);
        Map<String, Object> response = new HashMap<>();

        try {
            VideoEntity randomVideo = videoService.getSwipeVideo(videoLoc);

            if (randomVideo == null) {
                return ResponseEntity.ok(Map.of("message", "더 이상 시청할 영상이 없습니다.", "hasMore", false));
            }

            Long likeCnt = videoLikeService.countByVideo(randomVideo);
            Long commentCnt = commentService.countByVideo(randomVideo);
            UserEntity uploader = userService.findByMention(randomVideo.getUploader().getMention());

            if (user != null) {
                // viewStoryService.userViewstoryInsert(user.getId(), randomVideo.getId());
                videoService.incrementVideoViews(videoLoc, mention);
                boolean isLiked = videoLikeService.existsByVideoAndUser(randomVideo, user);
                boolean isFollowing = followService.existsByFollowUserAndFollowedUser(user, randomVideo.getUploader());

                response.put("isLiked", isLiked);
                response.put("isFollowing", isFollowing);
            }

            response.put("video", randomVideo);
            response.put("uploader", uploader);
            response.put("likeCnt", likeCnt);
            response.put("commentCnt", commentCnt);
            response.put("hasMore", true);
            response.put("id", randomVideo.getId());

            log.info("반환된 영상: ID={}, 제목={}", randomVideo.getId(), randomVideo.getVideoTitle());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("영상 로딩 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "영상을 불러오는 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/videos/tag")
    public ResponseEntity<List<?>> exploreVideoList(@RequestParam String hashtag) {
        return ResponseEntity.ok(videoService.selectExploreVideoListButTag(hashtag));
    }

    @GetMapping("/like/video")
    public ResponseEntity<List<IndexPageAllVideosDto>> userLikeVideoList(@RequestParam String mention) {
        UserEntity user = userService.findByMention(mention);
        return ResponseEntity.ok(videoService.myLikeVideos(user.getId()));
    }

    @GetMapping("/hashtag")
    public ResponseEntity<?> getTagVideo(@RequestParam String videoTag) {
        return ResponseEntity.ok(videoService.selectExploreVideoListButTag(videoTag));
    }









}

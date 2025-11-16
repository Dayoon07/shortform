package com.e.shortform.domain.video.service;

import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.repository.UserRepo;
import com.e.shortform.domain.video.entity.VideoEntity;
import com.e.shortform.domain.video.mapper.VideoMapper;
import com.e.shortform.domain.video.repository.VideoRepo;
import com.e.shortform.domain.video.res.IndexPageAllVideosDto;
import com.e.shortform.domain.video.res.VideoWithUserDto;
import com.e.shortform.domain.video.vo.VideoVo;
import com.e.shortform.domain.viewstory.entity.ViewStoryEntity;
import com.e.shortform.domain.viewstory.repository.ViewStoryRepo;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoService {

    private final UserRepo userRepo;
    private final VideoRepo videoRepo;
    private final VideoMapper videoMapper;
    private final ViewStoryRepo viewStoryRepo;

    public Map<String, Object> uploadVideo(
            MultipartFile file,
            String title,
            String description,
            String hashtags,
            String visibility,
            String commentsAllowed,
            MultipartFile thumbnail,
            UserEntity user) {
        Map<String, Object> response = new HashMap<>();

        try {
            String uploadPath = System.getProperty("user.home").replace("\\", "/")
                    + "/Desktop/shortform-server/shortform-user-video/";
            String previewImgUploadPath = System.getProperty("user.home").replace("\\", "/")
                    + "/Desktop/shortform-server/shortform-user-video-preview-img/";

            // 1. 디렉토리 생성
            File uploadDir = new File(uploadPath);
            File previewImgUploadDir = new File(previewImgUploadPath);

            if (!uploadDir.exists()) uploadDir.mkdirs();
            if (!previewImgUploadDir.exists()) previewImgUploadDir.mkdirs();

            // 2. 비디오 저장
            String videoExt = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
            String savedVideoName = UUID.randomUUID().toString() + videoExt;
            String videoFilePath = uploadPath + savedVideoName;

            File destinationVideo = new File(videoFilePath);
            file.transferTo(destinationVideo);

            log.info("비디오 저장 완료: {}", videoFilePath);

            // 3. 썸네일 저장
            String thumbnailSavedName = null;
            if (thumbnail != null && !thumbnail.isEmpty()) {
                String thumbExt = getFileExtension(Objects.requireNonNull(thumbnail.getOriginalFilename()));
                thumbnailSavedName = UUID.randomUUID().toString() + thumbExt;

                String thumbnailFilePath = previewImgUploadPath + thumbnailSavedName;
                File thumbnailFile = new File(thumbnailFilePath);

                thumbnail.transferTo(thumbnailFile);
                log.info("썸네일 저장 완료: {}", thumbnailFilePath);
            } else {
                log.warn("썸네일 파일이 비어있습니다.");
            }

            // 4. 사용자 확인
            UserEntity uploader = userRepo.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 5. 비디오 엔티티 생성 및 저장
            VideoEntity videoEntity = VideoEntity.builder()
                    .videoTitle(title)
                    .videoDescription(description)
                    .videoName(savedVideoName)
                    .videoSrc("/resources/shortform-user-video/" + savedVideoName)
                    .videoTag(hashtags)
                    .videoViews(0L)
                    .videoLoc(UUID.randomUUID().toString())
                    .uploader(uploader)
                    .videoWatchAvailability(visibility)
                    .commentAvailability(commentsAllowed)
                    .previewImg(thumbnailSavedName != null
                            ? "/resources/shortform-user-video-preview-img/" + thumbnailSavedName
                            : null)
                    .build();

            VideoEntity savedVideo = videoRepo.save(videoEntity);

            log.info("비디오 + 썸네일 DB 저장 완료: videoID={}", savedVideo.getId());

            // 6. 응답 구성
            response.put("success", true);
            response.put("message", "비디오 업로드가 완료되었습니다.");
            response.put("videoId", savedVideo.getId());
            response.put("fileName", savedVideoName);
            response.put("thumbnail", thumbnailSavedName);

        } catch (IOException e) {
            log.error("파일 저장 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "파일 저장 중 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("비디오 업로드 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "업로드 중 오류가 발생했습니다: " + e.getMessage());
        }

        return response;
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf("."));
    }

    public List<VideoEntity> findAllOrderByCreateAtDesc() {
        return videoRepo.findAll(Sort.by(Sort.Direction.DESC, "uploadAt"));
    }

    public VideoWithUserDto selectByVideo(String mention) {
        return videoMapper.selectByVideo(mention);
    }

    public VideoEntity findByVideoLoc(String videoLoc, HttpSession session) {
        VideoEntity video = videoRepo.findByVideoLoc(videoLoc);

        if (session.getAttribute("user") != null) {
            String key = "viewed_" + videoLoc;

            if (session.getAttribute(key) == null) {
                video.setVideoViews(video.getVideoViews() + 1);
                videoRepo.save(video);
                session.setAttribute(key, true); // 중복 조회 방지 설정
            }
        }

        return video;
    }

    public VideoEntity findByVideoLoc(String videoLoc, String currentUserMention) {
        UserEntity user = userRepo.findByMention(currentUserMention);
        VideoEntity video = videoRepo.findByVideoLoc(videoLoc);

        if (user != null) {
            // 이 비디오를 본 적이 있는지 확인
            boolean alreadyViewed = viewStoryRepo
                    .existsByUserAndVideo(user, video);

            if (!alreadyViewed) {
                // 조회수 증가
                video.setVideoViews(video.getVideoViews() + 1);
                videoRepo.save(video);

                // 시청 기록 저장
                ViewStoryEntity viewStory = new ViewStoryEntity();
                viewStory.setUser(user);
                viewStory.setVideo(video);
                viewStoryRepo.save(viewStory);
            }
        }

        return video;
    }

    public void incrementVideoViews(String videoLoc, String currentUserMention) {
        log.info("=== incrementVideoViews 호출 ===");
        log.info("videoLoc: {}, mention: {}", videoLoc, currentUserMention);

        UserEntity user = userRepo.findByMention(currentUserMention);
        VideoEntity video = videoRepo.findByVideoLoc(videoLoc);

        log.info("user: {}, video: {}", user != null ? user.getId() : "null",
                video != null ? video.getId() : "null");

        if (user != null && video != null) {
            boolean alreadyViewed = viewStoryRepo.existsByUserAndVideo(user, video);
            log.info("이미 본 영상? {}", alreadyViewed);

            if (!alreadyViewed) {
                video.setVideoViews(video.getVideoViews() + 1);
                videoRepo.save(video);
                log.info("✅ 조회수 증가: 비디오 ID={}, 조회수={}", video.getId(), video.getVideoViews());

                // 시청 기록 저장
                ViewStoryEntity viewStory = new ViewStoryEntity();
                viewStory.setUser(user);
                viewStory.setVideo(video);
                viewStoryRepo.save(viewStory);
                log.info("✅ 시청 기록 저장 완료");
            } else {
                log.info("⚠️ 이미 본 영상이므로 조회수 증가 안 함");
            }
        } else {
            log.warn("❌ user 또는 video가 null입니다!");
        }
    }

    /** 기존 메서드 (하휘 호환성 유지) */
    public List<IndexPageAllVideosDto> selectIndexPageAllVideos() {
        return videoMapper.selectIndexPageAllVideos();
    }

    public List<IndexPageAllVideosDto> selectUserProfilePageAllVideos(String mention) {
        return videoMapper.selectUserProfilePageAllVideos(mention);
    }

    public List<IndexPageAllVideosDto> searchLogic(String searchWordParam) {
        return videoMapper.searchLogic(searchWordParam);
    }

    public VideoEntity selectRandomVideo(List<Long> excludeIds) {
        VideoVo vo = videoMapper.selectRandomVideo(excludeIds);
        if (vo == null) {
            return null;
        }

        // VideoVo의 ID를 사용해서 VideoEntity를 조회
        Optional<VideoEntity> entity = videoRepo.findById(vo.getId()); // uploaderUserId가 아니라 video의 ID를 사용

        return entity.orElse(null);
    }

    public VideoEntity getSwipeVideo(String videoLoc) {
        VideoVo vo = videoMapper.getSwipeVideo(videoLoc);
        if (vo == null) {
            return null;
        }
        Optional<VideoEntity> entity = videoRepo.findById(vo.getId()); // uploaderUserId가 아니라 video의 ID를 사용

        return entity.orElse(null);
    }

    public List<VideoEntity> explorePageVideo() {
        return videoRepo.findAll(Sort.by(Sort.Direction.DESC, "uploadAt"));
    }

    public List<IndexPageAllVideosDto> myLikeVideos(Long id) {
        return videoMapper.myLikeVideos(id);
    }

    public List<IndexPageAllVideosDto> selectExploreVideoListButTag(String hashtag) {
        if (hashtag.equals("popular")) {
            return videoMapper.selectExploreVideoListByTagsButVideoViewsDescFuck(hashtag);
        } else {
            return videoMapper.selectExploreVideoListByTags(hashtag);
        }
    }

    public List<VideoEntity> selectAllVideos() {
        return videoRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public List<IndexPageAllVideosDto> selectExploreVideoListByTagsButVideoViewsDescFuck(String hashtag) {
        return videoMapper.selectExploreVideoListByTagsButVideoViewsDescFuck(hashtag);
    }

    public List<VideoEntity> findAll() {
        return videoRepo.findAll();
    }

    /**
     * 페이징된 비디오 목록 조회 (신규)
     */
    public Map<String, Object> selectIndexPageAllVideosPaginated(PageRequest pageRequest) {
        int page = pageRequest.getPageNumber();
        int size = pageRequest.getPageSize();
        int offset = page * size;

        // 전체 개수 조회
        int totalElements = videoMapper.countPublicVideos();

        // 페이징된 데이터 조회
        List<IndexPageAllVideosDto> content = videoMapper.selectIndexPageAllVideosPaginated(offset, size);

        // 총 페이지 수 계산
        int totalPages = (int) Math.ceil((double) totalElements / size);

        // 응답 데이터 구성
        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("totalElements", totalElements);
        response.put("totalPages", totalPages);
        response.put("number", page);
        response.put("size", size);
        response.put("numberOfElements", content.size());
        response.put("first", page == 0);
        response.put("last", page >= totalPages - 1);
        response.put("empty", content.isEmpty());

        return response;
    }

}

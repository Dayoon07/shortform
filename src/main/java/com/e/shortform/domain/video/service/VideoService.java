package com.e.shortform.domain.video.service;

import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.repository.UserRepo;
import com.e.shortform.domain.video.entity.VideoEntity;
import com.e.shortform.domain.video.mapper.VideoMapper;
import com.e.shortform.domain.video.repository.VideoRepo;
import com.e.shortform.domain.video.res.IndexPageAllVideosDto;
import com.e.shortform.domain.video.res.VideoWithUserDto;
import com.e.shortform.domain.video.vo.VideoVo;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public Map<String, Object> uploadVideo(
            MultipartFile file,
            String title,
            String description,
            String hashtags,
            String visibility,
            String commentsAllowed,
            UserEntity user) {

        Map<String, Object> response = new HashMap<>();

        try {
            String uploadPath = System.getProperty("user.home").replace("\\", "/") + "/Desktop/shortform-server/shortform-user-video/";
            // 1. 디렉토리 생성
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 2. 파일 저장
            String fileExtension = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
            String savedFileName = UUID.randomUUID().toString() + fileExtension;
            String filePath = uploadPath + savedFileName;

            File destinationFile = new File(filePath);
            file.transferTo(destinationFile);

            log.info("파일 저장 완료: {}", filePath);

            // 3. 사용자 확인
            UserEntity uploader = userRepo.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 4. 비디오 엔티티 생성 및 저장
            VideoEntity videoEntity = VideoEntity.builder()
                    .videoTitle(title)
                    .videoDescription(description)
                    .videoName(savedFileName)
                    .videoSrc("/resources/shortform-user-video/" + savedFileName)
                    .videoTag(hashtags)
                    .videoViews(0L)
                    .videoLoc(UUID.randomUUID().toString())
                    .uploader(uploader)
                    .videoWatchAvailability(visibility)
                    .commentAvailability(commentsAllowed)
                    .build();

            VideoEntity savedVideo = videoRepo.save(videoEntity);

            log.info("비디오 정보 DB 저장 완료: ID={}", savedVideo.getId());
            System.out.println("저장 경로 : " + destinationFile.getAbsolutePath());

            // 5. 응답 구성
            response.put("success", true);
            response.put("message", "비디오 업로드가 완료되었습니다.");
            response.put("videoId", savedVideo.getId());
            response.put("fileName", savedFileName);

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

}

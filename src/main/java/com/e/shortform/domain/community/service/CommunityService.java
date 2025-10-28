package com.e.shortform.domain.community.service;

import com.e.shortform.domain.community.res.CommunityWithUserProfileDto;
import com.e.shortform.domain.community.entity.CommunityAdditionEntity;
import com.e.shortform.domain.community.entity.CommunityEntity;
import com.e.shortform.domain.community.mapper.CommunityMapper;
import com.e.shortform.domain.community.repository.CommunityAdditionRepo;
import com.e.shortform.domain.community.repository.CommunityRepo;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.repository.UserRepo;
import com.e.shortform.model.dto.UserProfilePostAllLikeCntDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CommunityService {

    private final CommunityMapper communityMapper;

    private final UserRepo userRepo;
    private final CommunityRepo communityRepo;
    private final CommunityAdditionRepo communityAdditionRepo;

    // 업로드 설정 상수들
    private static final String UPLOAD_DIRECTORY = System.getProperty("user.home").replace("\\", "/") + "/Desktop/shortform-server/shortform-community-post-img";
    private static final String BASE_URL = "/resources/shortform-community-post-img";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_FILES = 5;

    // 허용된 파일 확장자
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    // 파일명에 사용할 날짜 포맷터
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    public ResponseEntity<Map<String, Object>> realCreatePost(String content, String visibility,
        List<MultipartFile> images, HttpSession session) {
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
            String result = createPost(content, visibility, images, session);

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

    /**
     * 커뮤니티 게시글을 생성합니다.
     *
     * @param content 게시글 내용
     * @param visibility 공개 범위
     * @param images 첨부 이미지들
     * @param session HTTP 세션
     * @return 생성 결과 메시지
     * @throws IllegalArgumentException 잘못된 입력이 있을 경우
     * @throws SecurityException 권한이 없을 경우
     */
    public String createPost(String content, String visibility,
                             List<MultipartFile> images, HttpSession session) {

        // 입력 검증 (글만, 이미지만, 글+이미지 모든 케이스 지원)
        validateInput(content, visibility, images);

        // 사용자 검증
        UserEntity user = validateAndGetUser(session);

        try {
            // 게시글 생성 및 저장 - 내용이 없을 수도 있음을 고려
            CommunityEntity savedPost = createAndSavePost(content, visibility, user);

            // 이미지 처리
            if (hasImages(images)) {
                processImages(images, savedPost);
            }

            // 로그 메시지도 업데이트
            String postType = getPostType(content, images);
            log.info("커뮤니티 게시글 생성 완료 - ID: {}, 사용자: {}, 유형: {}, 이미지 수: {}",
                    savedPost.getId(), user.getId(), postType, getImageCount(images));

            return "커뮤니티 글이 성공적으로 작성되었습니다";

        } catch (Exception e) {
            log.error("커뮤니티 게시글 생성 중 오류 발생 - 사용자: {}", user.getId(), e);
            throw new RuntimeException("게시글 작성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 게시글 유형을 반환합니다.
     */
    private String getPostType(String content, List<MultipartFile> images) {
        boolean hasContent = content != null && !content.trim().isEmpty();
        boolean hasImages = hasImages(images);

        if (hasContent && hasImages) {
            return "텍스트+이미지";
        } else if (hasContent) {
            return "텍스트만";
        } else if (hasImages) {
            return "이미지만";
        } else {
            return "빈 게시글"; // 이 경우는 검증에서 걸러져야 함
        }
    }

    /**
     * 입력값들을 검증합니다.
     * 글만 있는 경우, 이미지만 있는 경우, 글+이미지 모두 있는 경우를 지원
     */
    private void validateInput(String content, String visibility, List<MultipartFile> images) {
        // 내용과 이미지 존재 여부 확인
        boolean hasContent = content != null && !content.trim().isEmpty();
        boolean hasImages = hasImages(images);

        // 내용과 이미지 중 하나라도 있어야 함
        if (!hasContent && !hasImages) {
            throw new IllegalArgumentException("게시글 내용 또는 이미지 중 하나는 입력해주세요");
        }

        // 내용이 있을 경우 길이 검증
        if (hasContent && content.trim().length() > 2000) {
            throw new IllegalArgumentException("게시글 내용은 2000자 이하로 작성해주세요");
        }

        // 공개범위 검증
        if (visibility == null || visibility.trim().isEmpty()) {
            throw new IllegalArgumentException("공개 범위를 선택해주세요");
        }

        if (!Arrays.asList("public", "private", "followers").contains(visibility)) {
            throw new IllegalArgumentException("올바른 공개 범위를 선택해주세요");
        }

        // 이미지 개수 검증
        if (images != null && images.size() > MAX_FILES) {
            throw new IllegalArgumentException(String.format("이미지는 최대 %d개까지 업로드 가능합니다", MAX_FILES));
        }

        // 이미지 파일 검증
        if (hasImages) {
            validateImages(images);
        }
    }

    /**
     * 이미지 파일들을 검증합니다.
     */
    private void validateImages(List<MultipartFile> images) {
        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                continue;
            }

            // 파일 크기 검증
            if (image.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("이미지 파일 크기는 5MB 이하여야 합니다");
            }

            // 파일 타입 검증
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다");
            }

            // 파일 확장자 검증
            String originalFilename = image.getOriginalFilename();
            if (originalFilename == null || !hasValidExtension(originalFilename)) {
                throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (jpg, jpeg, png, gif, webp만 가능)");
            }
        }
    }

    /**
     * 유효한 파일 확장자인지 확인합니다.
     */
    private boolean hasValidExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return false;
        }
        String extension = filename.substring(lastDotIndex).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    /**
     * 사용자를 검증하고 반환합니다.
     */
    private UserEntity validateAndGetUser(HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");

        if (user == null) {
            throw new SecurityException("로그인이 필요합니다");
        }

        // 사용자 존재 여부 확인
        return userRepo.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다"));
    }

    /**
     * 게시글을 생성하고 저장합니다.
     * 내용이 비어있을 수 있음을 고려하여 수정
     */
    private CommunityEntity createAndSavePost(String content, String visibility, UserEntity user) {
        // content가 null이거나 비어있으면 빈 문자열로 처리
        String processedContent = (content != null && !content.trim().isEmpty())
                ? content.trim()
                : "";

        CommunityEntity post = CommunityEntity.builder()
                .communityText(processedContent)
                .user(user)
                .communityUuid(generateUniqueUuid())
                .communityAvailability(visibility)
                .build();

        return communityRepo.save(post);
    }

    /**
     * 유일한 UUID를 생성합니다.
     */
    private String generateUniqueUuid() {
        String uuid;
        do {
            uuid = UUID.randomUUID().toString();
        } while (communityRepo.existsByCommunityUuid(uuid));

        return uuid;
    }

    /**
     * 이미지들을 처리합니다.
     */
    private void processImages(List<MultipartFile> images, CommunityEntity post) {
        try {
            // 업로드 디렉토리 생성
            createUploadDirectory();

            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);
                if (!image.isEmpty()) {
                    saveImageFile(image, post, i);
                }
            }
        } catch (IOException e) {
            log.error("이미지 처리 중 IO 오류 발생 - 게시글 ID: {}", post.getId(), e);
            throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다", e);
        }
    }

    /**
     * 업로드 디렉토리를 생성합니다.
     */
    private void createUploadDirectory() throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIRECTORY);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("업로드 디렉토리 생성: {}", uploadPath);
        }
    }

    /**
     * 이미지 파일을 저장합니다.
     */
    private void saveImageFile(MultipartFile image, CommunityEntity post, int index) throws IOException {
        String fileName = generateFileName(image.getOriginalFilename());
        Path filePath = Paths.get(UPLOAD_DIRECTORY, fileName);

        // 파일 저장
        Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // DB에 파일 정보 저장
        saveCommunityAddition(fileName, post);

        log.debug("이미지 파일 저장 완료 - 파일명: {}, 게시글 ID: {}", fileName, post.getId());
    }

    /**
     * 고유한 파일명을 생성합니다.
     */
    private String generateFileName(String originalFilename) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String uuid = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "");
        String extension = extractFileExtension(originalFilename);

        return timestamp + "-" + uuid + extension;
    }

    /**
     * 파일 확장자를 추출합니다.
     */
    private String extractFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }

    /**
     * CommunityAdditionEntity를 저장합니다.
     */
    private void saveCommunityAddition(String fileName, CommunityEntity post) {
        CommunityAdditionEntity additionEntity = CommunityAdditionEntity.builder()
                .fileSrc(BASE_URL + "/" + fileName)
                .community(post)
                .build();

        communityAdditionRepo.save(additionEntity);
    }

    /**
     * 이미지가 있는지 확인합니다.
     */
    private boolean hasImages(List<MultipartFile> images) {
        return images != null && !images.isEmpty() &&
                images.stream().anyMatch(image -> !image.isEmpty());
    }

    /**
     * 이미지 개수를 반환합니다.
     */
    private int getImageCount(List<MultipartFile> images) {
        if (images == null) {
            return 0;
        }
        return (int) images.stream().filter(image -> !image.isEmpty()).count();
    }

    public List<CommunityWithUserProfileDto> selectByCommunityButWhereId(Long id) {
        return communityMapper.selectByCommunityButWhereId(id);
    }

    public List<CommunityEntity> selectAllCommunity() {
        return communityRepo.findAll();
    }

    public CommunityEntity findByCommunityUuid(String communityUuid) {
        return communityRepo.findByCommunityUuid(communityUuid);
    }

    public UserProfilePostAllLikeCntDto findByCommunityBoardFuck(String uuid) {
        return communityMapper.findByCommunityBoardFuck(uuid);
    }

    public List<UserProfilePostAllLikeCntDto> selectByCommunityButWhereIdAsdf(Long id) {
        return communityMapper.selectByCommunityButWhereIdAsdf(id);
    }

}
package com.e.shortform.domain.user.service;

import com.e.shortform.config.JwtUtil;
import com.e.shortform.config.oauth.TokenBlacklistService;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.enums.SocialProviderStatus;
import com.e.shortform.domain.user.mapper.UserMapper;
import com.e.shortform.domain.user.repository.UserRepo;
import com.e.shortform.domain.user.res.UserProfileDto;
import com.e.shortform.domain.user.res.UserProfileUpdateDto;
import com.e.shortform.domain.user.vo.UserVo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final BCryptPasswordEncoder passwordEncoder;

    private final UserMapper userMapper;
    private final UserRepo userRepo;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    public List<UserVo> selectAll() {
        return userMapper.selectAll();
    }

    public List<UserEntity> selectAllUsers() {
        return userRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public boolean selectChkUsername(String username) {
        return userMapper.selectChkUsername(username) == null;
    }

    public boolean selectChkUserMail(String mail) {
        return userMapper.selectChkUserMail(mail) == null;
    }

    public String signup(String username, String password, String mail, MultipartFile file) {
        try {
            String n = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
            String originalFilename = file.getOriginalFilename();
            String exten = originalFilename.substring(originalFilename.lastIndexOf("."));

            String fileName = n + UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "") + exten;
            String uploadDir = System.getProperty("user.home").replace("\\", "/") + "/Desktop/shortform-server/shortform-user-profile-img/";

            String mentionUuid = "user-" + UUID.randomUUID().toString().substring(0, 28);

            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File destFile = new File(dir, fileName);
            file.transferTo(destFile);
            System.out.println(destFile.getAbsolutePath());

            UserEntity userEntity = UserEntity.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .mail(mail)
                    .profileImg(fileName)
                    .profileImgSrc("/resources/shortform-user-profile-img/" + fileName)
                    .mention(mentionUuid)
                    .social(false)
                    .provider(SocialProviderStatus.LOCAL.name())
                    .build();

            userRepo.save(userEntity);
            return "회원가입에 성공했습니다";
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return "회원가입에 실패했습니다";
        }
    }

    public UserEntity findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public ResponseEntity<Map<String, Object>> login(
            String username,
            String password,
            HttpSession session,    // 제거 예정
            @RequestHeader(value = "X-Client-Type", required = false) String clientType
    ) {
        Map<String, Object> response = new HashMap<>();

        if (username == null || username.trim().isEmpty())
            throw new IllegalArgumentException("사용자명을 입력해주세요.");

        if (password == null || password.trim().isEmpty())
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");

        UserEntity user = userRepo.findByUsername(username);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            response.put("success", false);
            response.put("message", "사용자명 또는 비밀번호가 올바르지 않습니다.");
            throw new SecurityException("사용자명 또는 비밀번호가 올바르지 않습니다.");
        }

        try {
            String token = jwtUtil.generateToken(user);

            response.put("success", true);
            response.put("message", "로그인 되었습니다");
            response.put("token", token);
            response.put("tokenType", "Bearer");
            response.put("user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "mail", user.getMail(),
                    "profileImgSrc", user.getProfileImgSrc(),
                    "mention", user.getMention(),
                    "createAt", user.getCreateAt(),
                    "isSocial", user.isSocial(),
                    "provider", user.getProvider()
            ));

            System.out.println(getClass().getName() + " - " + response);

            if ("mobile".equals(clientType)) {
                // 모바일/네이티브 앱: JWT 토큰 발행
                String token2 = jwtUtil.generateToken(user);
                response.put("token", token2);
                response.put("tokenType", "Bearer");
            } else {
                // 웹 애플리케이션: 세션 사용
                session.setAttribute("user", user);
            }
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
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<Map<String, Object>> logout(HttpSession session,
                                                      HttpServletResponse servletRes,
                                                      String bearerToken) {
        Map<String, Object> response = new HashMap<>();

        try {
            String token = jwtUtil.extractTokenFromBearer(bearerToken);

            if (session != null) {
                session.invalidate();
            }

            if (token == null) {
                response.put("success", false);
                response.put("message", "유효하지 않은 토큰입니다");
                return ResponseEntity.badRequest().body(response);
            } else {
                // 블랙리스트에 추가
                tokenBlacklistService.addToBlacklist(token);

                response.put("success", true);
                response.put("message", "로그아웃되었습니다");

                Cookie cookie = new Cookie("accessTkn", null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                cookie.setHttpOnly(false);
                cookie.setSecure(false);
                servletRes.addCookie(cookie);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류", e);
            response.put("success", false);
            response.put("message", "로그아웃 처리 중 오류가 발생했습니다");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public UserEntity findByMention(String mention) {
        return userRepo.findByMention(mention);
    }

    public UserProfileDto getUserProfilePageInfo(Long id) {
        return userMapper.getUserProfilePageInfo(id);
    }

    public List<UserVo> selectProfileUserFollowList(Long id) {
        return userMapper.selectProfileUserFollowList(id);
    }

    public List<UserVo> selectProfileUserFollowingList(Long id) {
        return userMapper.selectProfileUserFollowingList(id);
    }

    // updateUserInfo 메서드 시그니처 변경
    public void updateUserInfo(UserProfileUpdateDto dto, String newProfileImgPath, Long userId) {
        // DTO의 필드가 null이 아닐 때만 업데이트하는 로직을 Mapper나 Service에 구현합니다.
        userMapper.updateUserInfo(
                dto.getUsername(),
                dto.getMail(),
                dto.getMention(),
                dto.getBio(),
                // 파일 경로는 DTO가 아닌 Controller에서 처리된 최종 경로를 사용
                newProfileImgPath, // finalProfileImgPath
                newProfileImgPath, // profileImgSrc
                userId // AuthUserReqDto에서 가져온 ID를 사용
        );
    }

    public String fileTransfer(MultipartFile profileImg, String currentProfileImgSrc) {
        if (profileImg != null && !profileImg.isEmpty()) {
            String newFilename = UUID.randomUUID().toString() + profileImg.getOriginalFilename().substring(profileImg.getOriginalFilename().lastIndexOf("."));
            String uploadDir = System.getProperty("user.home").replace("\\", "/") + "/Desktop/shortform-server/shortform-user-profile-img/";
            Path savePath = Paths.get(uploadDir, newFilename);

            try {
                profileImg.transferTo(savePath);
                return "/resources/shortform-user-profile-img/" + newFilename;
            } catch (IOException e) {
                throw new RuntimeException("프로필 이미지 저장 실패", e);
            }
        } else {
            return extractPathFromUrl(currentProfileImgSrc);
        }
    }

    private String extractPathFromUrl(String fullUrl) {
        if (fullUrl == null || fullUrl.isEmpty()) return null;

        try {
            return new URL(fullUrl).getPath();
        } catch (MalformedURLException e) {
            int index = fullUrl.indexOf("/resources/");
            return (index != -1) ? fullUrl.substring(index) : fullUrl;
        }
    }

    public UserEntity findById(Long id) {
        return userRepo.findById(id).orElseThrow();
    }

    public UserEntity findByMail(String mail) {
        return userRepo.findByMail(mail);
    }

    /** 구글 로그인 시 호출하는 회원가입 기능입니다 */
    public UserEntity createSocialUser(String email, String name, String picture, String provider) {
        String mentionUuid = "user-" + UUID.randomUUID().toString().substring(0, 28);

        UserEntity userEntity = UserEntity.builder()
                .username(name)
                .mail(email)
                .password(passwordEncoder.encode("1234"))  // 소셜 로그인은 비밀번호가 필요하지 않으나 null값을 넣을 수는 없기에 하드코딩함
                .profileImg(picture != null ? picture : "default.jpg")
                .profileImgSrc(picture != null ? picture : "/resources/shortform-user-profile-img/default.jpg")
                .mention(mentionUuid)
                .social(true)            // 소셜 회원
                .provider(provider)      // google, naver, kakao 등
                .build();

        return userRepo.save(userEntity);
    }

    /** 사용자 정보 업데이트 (주로 소셜 연동시 사용) */
    public void updateUser(UserEntity user) {
        userRepo.save(user);
    }

    /** 로컬 계정에서 소셜 계정 연동 */
    public void linkSocialAccount(Long userId, String provider) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.setSocial(true);
        user.setProvider(provider);
        userRepo.save(user);
    }

    /** 소셜 계정 여부 확인 */
    public boolean isSocialUser(Long userId) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return user.isSocial();
    }

    /** 제공자 정보 조회 */
    public String getUserProvider(Long userId) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return user.getProvider();
    }

}

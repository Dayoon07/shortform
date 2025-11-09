package com.e.shortform.domain.user.service;

import com.e.shortform.config.JwtUtil;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.mapper.UserMapper;
import com.e.shortform.domain.user.repository.UserRepo;
import com.e.shortform.domain.user.res.UserProfileDto;
import com.e.shortform.domain.user.res.UserProfileUpdateDto;
import com.e.shortform.domain.user.vo.UserVo;
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

    public List<UserVo> selectAll() {
        return userMapper.selectAll();
    }

    public List<UserEntity> selectAllUsers() {
        return userRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public String selectChkUsername(String username) {
        return userMapper.selectChkUsername(username);
    }

    public String selectChkUserMail(String mail) {
        return userMapper.selectChkUserMail(mail);
    }

    public void signup(String username, String password, String mail, MultipartFile file) {
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
                    .build();

            userRepo.save(userEntity);

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("파일 업로드 실패");
        }
    }

    public UserEntity findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public ResponseEntity<Map<String, Object>> login(String username, String password, HttpSession session,
        @RequestHeader(value = "X-Client-Type", required = false) String clientType) {
        Map<String, Object> response = new HashMap<>();

        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자명을 입력해주세요.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        UserEntity user = userRepo.findByUsername(username);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            response.put("success", false);
            response.put("message", "사용자명 또는 비밀번호가 올바르지 않습니다.");
            throw new SecurityException("사용자명 또는 비밀번호가 올바르지 않습니다.");
        }

        try {
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

    public void updateUserInfo(UserProfileUpdateDto dto) {
        userMapper.updateUserInfo(
                dto.username(),
                dto.mail(),
                dto.mention(),
                dto.bio(),
                dto.profileImg(),
                dto.profileImgSrc(),
                dto.id()
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

}

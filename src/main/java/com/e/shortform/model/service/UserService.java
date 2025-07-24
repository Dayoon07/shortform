package com.e.shortform.model.service;

import com.e.shortform.model.entity.UserEntity;
import com.e.shortform.model.mapper.UserMapper;
import com.e.shortform.model.repository.UserRepo;
import com.e.shortform.model.vo.UserVo;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final BCryptPasswordEncoder passwordEncoder;

    private final UserMapper userMapper;
    private final UserRepo userRepo;

    public List<UserVo> selectAll() {
        return userMapper.selectAll();
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

    public UserEntity login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("사용자명을 입력해주세요.");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        UserEntity user = userRepo.findByUsername(username);

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new SecurityException("사용자명 또는 비밀번호가 올바르지 않습니다.");
        }

        return user;
    }


}

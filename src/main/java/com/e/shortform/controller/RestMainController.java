package com.e.shortform.controller;

import com.e.shortform.model.entity.UserEntity;
import com.e.shortform.model.service.FollowService;
import com.e.shortform.model.service.UserService;
import com.e.shortform.model.service.VideoService;
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

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/api")
public class RestMainController {

    private final UserService userService;
    private final VideoService videoService;
    private final FollowService followService;

    private final PasswordEncoder passwordEncoder;

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
    public ResponseEntity<Map<String, Object>> uploadVideoComplete(
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

}

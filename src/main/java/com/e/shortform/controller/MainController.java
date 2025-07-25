package com.e.shortform.controller;

import com.e.shortform.model.service.UserService;
import com.e.shortform.model.service.VideoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MainController {

    private final UserService userService;
    private final VideoService videoService;

    @GetMapping("/")
    public String index(Model m) {
        m.addAttribute("videos", videoService.selectIndexPageAllVideos());
        return "index";
    }

    @GetMapping("/search")
    public String search(Model m, @RequestParam String q) {
        m.addAttribute("searchWord", q);
        return "search/search";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/@{mention}")
    public String profilePage(@PathVariable String mention, Model m, HttpSession session) {
        m.addAttribute("profileInfo", userService.getUserProfilePageInfo(userService.findByMention(mention).getId()));
        return "profile/profile";
    }

    @GetMapping("/studio/upload")
    public String uploadPage(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "index";
        }
        return "video/upload";
    }

    @GetMapping("/@{mention}/video/{videoLoc}")
    public String videoPage(@PathVariable String mention, @PathVariable String videoLoc, Model m, HttpSession session) {
        m.addAttribute("videoInfo", videoService.findByVideoLoc(videoLoc, session));
        return "video/video";
    }

}

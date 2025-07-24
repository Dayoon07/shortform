package com.e.shortform.controller;

import com.e.shortform.model.entity.UserEntity;
import com.e.shortform.model.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MainController {

    private final UserService userService;

    @GetMapping("/")
    public String index(HttpServletRequest req, Model m) {
        m.addAttribute("currentPath", req.getRequestURI());
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
    public String uploadPage() {
        return "video/upload";
    }

}

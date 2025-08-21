package com.e.shortform.controller;

import com.e.shortform.model.entity.UserEntity;
import com.e.shortform.model.service.*;
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
    private final FollowService followService;
    private final CommentService commentService;
    private final VideoLikeService videoLikeService;
    private final SearchListService searchListService;

    @GetMapping("/")
    public String index(Model m) {
        m.addAttribute("videos", videoService.selectIndexPageAllVideos());
        return "index";
    }

    @GetMapping("/search")
    public String searchPage(@RequestParam String q, HttpSession session, Model m) {
        if (session.getAttribute("user") != null) {
            searchListService.searchWordRecord(q, session);
        } else {
            searchListService.searchWordRecord(q);
        }

        m.addAttribute("videos", videoService.searchLogic(q));
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
        UserEntity profileUser = userService.findByMention(mention);
        UserEntity currentUser = (UserEntity) session.getAttribute("user");

        if (profileUser != null) {
            // 기본 프로필 정보
            m.addAttribute("profileInfo", userService.getUserProfilePageInfo(profileUser.getId()));
            m.addAttribute("profileUserVideoInfo", videoService.selectUserProfilePageAllVideos(mention));

            // 팔로우 상태 확인 (로그인한 사용자가 있을 때만)
            if (currentUser != null && !currentUser.getId().equals(profileUser.getId())) {
                boolean isFollowing = followService.isFollowing(currentUser.getId(), profileUser.getId());
                m.addAttribute("isFollowing", isFollowing);
            } else {
                m.addAttribute("isFollowing", false);
            }
        }

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
        UserEntity user = (UserEntity) session.getAttribute("user");

        m.addAttribute("videoInfo", videoService.findByVideoLoc(videoLoc, session));
        m.addAttribute("videoCommentSize", commentService.selectByCommentId(videoService.findByVideoLoc(videoLoc, session).getId()).size());

        VideoLikeService.VideoLikeInfo likeInfo = videoLikeService.getVideoLikeInfoWithMyBatis(
                videoService.findByVideoLoc(videoLoc, session).getId(),
                user != null ? user.getId() : null
        );

        m.addAttribute("likeCount", likeInfo.getLikeCount());
        m.addAttribute("isLiked", likeInfo.isLiked());
        return "video/video";
    }

    @GetMapping("/@{mention}/post")
    public String profilePostPage(@PathVariable String mention, Model m, HttpSession session) {
        m.addAttribute("profileInfo", userService.getUserProfilePageInfo(userService.findByMention(mention).getId()));
        m.addAttribute("profileUserVideoInfo", videoService.selectUserProfilePageAllVideos(mention));
        return "profile/profile-post";
    }

    @GetMapping("/following")
    public String followingPage(HttpSession session, Model m) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user == null) {
            return "index";
        }

        m.addAttribute("followList", userService.selectProfileUserFollowingList(user.getId()));
        return "follow/following";
    }

    @GetMapping("/explore")
    public String explorePage(Model m) {
        m.addAttribute("videos",  videoService.selectIndexPageAllVideos());
        return "explore/explore";
    }

    @GetMapping("/@{mention}/swipe/video/{videoLoc}")
    public String profileUserVideoPage(@PathVariable String mention, @PathVariable String videoLoc, Model m, HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");

        m.addAttribute("videoInfo", videoService.findByVideoLoc(videoLoc, session));
        m.addAttribute("videoCommentSize", commentService.selectByCommentId(videoService.findByVideoLoc(videoLoc, session).getId()).size());

        VideoLikeService.VideoLikeInfo likeInfo = videoLikeService.getVideoLikeInfoWithMyBatis(
                videoService.findByVideoLoc(videoLoc, session).getId(),
                user != null ? user.getId() : null
        );

        m.addAttribute("likeCount", likeInfo.getLikeCount());
        m.addAttribute("isLiked", likeInfo.isLiked());
        return "video/swipe-video";
    }

    @GetMapping("/likes")
    public String likesPage(HttpSession session, Model m) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user == null) return "loginplz/loginplz";

        m.addAttribute("likeVideos", videoService.myLikeVideos(user.getId()));
        return "like/like";
    }

}

package com.e.shortform.controller;

import com.e.shortform.model.entity.CommunityEntity;
import com.e.shortform.model.entity.UserEntity;
import com.e.shortform.model.entity.VideoEntity;
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
    private final ViewStoryService viewStoryService;
    private final CommunityService communityService;
    private final CommunityAdditionService communityAdditionService;
    private final CommunityLikeService communityLikeService;

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
        m.addAttribute("searchWord", q.length() > 12 ? q.substring(0, 12) + "..." : q);
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
            m.addAttribute("profileInfo", userService.getUserProfilePageInfo(profileUser.getId()));
            m.addAttribute("profileUserVideoInfo", videoService.selectUserProfilePageAllVideos(mention));

            // 팔로우 상태 확인 (로그인한 사용자가 있을 때만)
            if (currentUser != null && !currentUser.getId().equals(profileUser.getId())) {
                boolean isFollowing = followService.isFollowing(currentUser.getId(), profileUser.getId());
                m.addAttribute("isFollowing", isFollowing);
            } else {
                m.addAttribute("isFollowing", false);
            }

            return "profile/profile";
        } else {
            return "profile/void-user";
        }
    }

    @GetMapping("/studio/upload")
    public String uploadPage(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "loginplz/loginplz";
        } else {
            return "video/upload";
        }
    }

    @GetMapping("/@{mention}/video/{videoLoc}")
    public String videoPage(@PathVariable String mention, @PathVariable String videoLoc, Model m, HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        VideoEntity video = videoService.findByVideoLoc(videoLoc, session);

        VideoLikeService.VideoLikeInfo likeInfo = videoLikeService.getVideoLikeInfoWithMyBatis(
                videoService.findByVideoLoc(videoLoc, session).getId(),
                user != null ? user.getId() : null
        );

        if (user != null) viewStoryService.userViewstoryInsert(user.getId(), video.getId());

        m.addAttribute("videoInfo", video);
        m.addAttribute("videoCommentSize", commentService.selectByCommentId(videoService.findByVideoLoc(videoLoc, session).getId()).size());
        m.addAttribute("likeCount", likeInfo.getLikeCount());
        m.addAttribute("isLiked", likeInfo.isLiked());

        return "video/video";
    }

    @GetMapping("/@{mention}/post")
    public String profilePostPage(@PathVariable String mention, Model m, HttpSession session) {
        UserEntity profileUser = userService.findByMention(mention);
        UserEntity currentUser = (UserEntity) session.getAttribute("user");

        if (profileUser != null) {
            m.addAttribute("profileInfo", userService.getUserProfilePageInfo(profileUser.getId()));
            m.addAttribute("profileUserVideoInfo", videoService.selectUserProfilePageAllVideos(mention));

            // 팔로우 상태 확인 (로그인한 사용자가 있을 때만)
            if (currentUser != null && !currentUser.getId().equals(profileUser.getId())) {
                boolean isFollowing = followService.isFollowing(currentUser.getId(), profileUser.getId());
                m.addAttribute("isFollowing", isFollowing);
            } else {
                m.addAttribute("isFollowing", false);
            }

            m.addAttribute("posts", communityService.selectByCommunityButWhereIdAsdf(profileUser.getId()));
            return "profile/profile-post";
        } else {
            return "profile/void-user";
        }
    }

    @GetMapping("/following")
    public String followingPage(HttpSession session, Model m) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user == null) return "loginplz/loginplz";

        m.addAttribute("followList", userService.selectProfileUserFollowingList(user.getId()));
        return "follow/following";
    }

    @GetMapping("/explore")
    public String explorePage(Model m) {
        m.addAttribute("videos", videoService.selectIndexPageAllVideos());
        return "explore/explore";
    }

    @GetMapping("/@{mention}/swipe/video/{videoLoc}")
    public String profileUserVideoPage(@PathVariable String mention, @PathVariable String videoLoc, Model m, HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        VideoEntity video = videoService.findByVideoLoc(videoLoc, session);
        UserEntity followUser = userService.findByMention(mention);

        // 비디오나 사용자가 없으면 에러 처리
        if (video == null || followUser == null) return "error/404";

        // 팔로우 상태 확인 - 로직 개선
        boolean isFollowing = false;
        if (user != null && !user.getId().equals(followUser.getId())) {
            isFollowing = followService.isFollowing(user.getId(), followUser.getId());
        }
        m.addAttribute("isFollowing", isFollowing);
        m.addAttribute("followUser", followUser); // 팔로우 대상 사용자 정보 추가

        // 비디오 좋아요 정보
        VideoLikeService.VideoLikeInfo likeInfo = videoLikeService.getVideoLikeInfoWithMyBatis(
                video.getId(),
                user != null ? user.getId() : null
        );

        // 조회 기록 추가
        if (user != null) viewStoryService.userViewstoryInsert(user.getId(), video.getId());

        m.addAttribute("videoInfo", video);
        m.addAttribute("videoCommentSize", commentService.selectByCommentId(video.getId()).size());
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

    @GetMapping("/loginplz")
    public String loginplzPage() {
        return "loginplz/loginplz";
    }

    @GetMapping("/hashtag/{videoTag}")
    public String hashtagPage(@PathVariable String videoTag, Model m) {
        m.addAttribute("hashtagVideo", videoService.selectExploreVideoListButTag(videoTag));
        return "hashtag/hashtag";
    }

    @GetMapping("/studio/post/write")
    public String postWritePage(HttpSession s) {
        if (s.getAttribute("user") == null) return "loginplz/loginplz";
        return "profile/post-write";
    }

    @GetMapping("/@{mention}/post/{communityUuid}")
    public String communityArticlePage(@PathVariable String mention, @PathVariable String communityUuid, Model m) {
        UserEntity user = userService.findByMention(mention);
        CommunityEntity community = communityService.findByCommunityUuid(communityUuid);

        if (user == null) {
            throw new IllegalArgumentException("존재하지 않는 유저입니다.");
        }
        if (community == null) {
            throw new IllegalArgumentException("존재하지 않는 게시글입니다.");
        }

        m.addAttribute("cat", communityService.findByCommunityBoardFuck(communityUuid));
        return "profile/post-detail";
    }

}

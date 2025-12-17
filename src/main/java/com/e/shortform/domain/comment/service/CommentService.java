package com.e.shortform.domain.comment.service;

import com.e.shortform.domain.comment.entity.CommentEntity;
import com.e.shortform.domain.comment.mapper.CommentMapper;
import com.e.shortform.domain.comment.repository.CommentRepo;
import com.e.shortform.domain.comment.res.CommentWithUserVideoDto;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.repository.UserRepo;
import com.e.shortform.domain.video.entity.VideoEntity;
import com.e.shortform.domain.video.repository.VideoRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepo commentRepo;
    private final UserRepo userRepo;
    private final VideoRepo videoRepo;

    private final CommentMapper commentMapper;

    public Map<String, Object> videoInsertComment(String commentText, Long commentUserId, Long commentVideoId) {
        UserEntity user = userRepo.findById(commentUserId).orElse(null);
        VideoEntity video = videoRepo.findById(commentVideoId).orElse(null);
        Map<String, Object> map = new HashMap<>();

        try {
            CommentEntity commentEntity = CommentEntity.builder()
                    .commentText(commentText)
                    .user(user)
                    .video(video)
                    .deleteStatus(false)
                    .build();

            commentRepo.save(commentEntity);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }

        map.put("commentText", commentText);
        map.put("userObj", user);
        return map;
    }

    public List<CommentWithUserVideoDto> selectByCommentId(Long id) {
        return commentMapper.selectByCommentId(id);
    }

    public List<CommentWithUserVideoDto> selectByCommentButOrderByIsDesc(Long id) {
        return commentMapper.selectByCommentButOrderByIsDesc(id);
    }

    public List<CommentEntity> selectAllComments() {
        return commentRepo.findAll();
    }

    public long countByVideo(VideoEntity video) {
        return commentRepo.countByVideo(video);
    }

}

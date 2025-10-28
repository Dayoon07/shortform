package com.e.shortform.domain.comment.service;

import com.e.shortform.domain.comment.entity.CommentEntity;
import com.e.shortform.domain.comment.entity.CommentLikeEntity;
import com.e.shortform.domain.comment.repository.CommentLikeRepo;
import com.e.shortform.domain.comment.repository.CommentRepo;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.repository.UserRepo;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentService commentService;
    private final CommentRepo commentRepo;
    private final UserRepo userRepo;
    private final CommentLikeRepo commentLikeRepo;

    @Transactional
    public boolean toggleCommentLike(Long commentId, HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user == null) {
            throw new IllegalStateException("로그인 필요");
        }

        CommentEntity comment = commentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글"));

        // 이미 좋아요 되어 있는지 확인
        Optional<CommentLikeEntity> existing = commentLikeRepo.findByUserAndComment(user, comment);

        if (existing.isPresent()) {
            // 이미 좋아요 → 삭제
            commentLikeRepo.delete(existing.get());
            return false; // unliked
        } else {
            // 좋아요 새로 추가
            CommentLikeEntity commentLikeEntity = CommentLikeEntity.builder()
                    .comment(comment)
                    .user(user)
                    .build();
            commentLikeRepo.save(commentLikeEntity);
            return true; // liked
        }
    }





}

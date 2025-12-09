package com.e.shortform.domain.comment.service;

import com.e.shortform.domain.comment.entity.CommentEntity;
import com.e.shortform.domain.comment.entity.CommentReplyEntity;
import com.e.shortform.domain.comment.repository.CommentReplyRepo;
import com.e.shortform.domain.comment.repository.CommentRepo;
import com.e.shortform.domain.comment.req.CommentReplyReqDto;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.repository.UserRepo;
import com.e.shortform.domain.user.req.AuthUserReqDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentReplyService {

    private final CommentReplyRepo commentReplyRepo;
    private final CommentRepo commentRepo;
    private final UserRepo userRepo;

    public void commentReplyInsert(CommentReplyReqDto dto, AuthUserReqDto u) throws Exception {
        UserEntity user = userRepo.findByMention(u.getMention());
        if (user == null) throw new Exception("로그인이 필요합니다");

        CommentEntity parentComment = commentRepo.findById(dto.getCommentReplyId())
                .orElseThrow(() -> new Exception("댓글을 찾을 수 없습니다"));

        CommentReplyEntity entity = CommentReplyEntity.builder()
                .commentReplyText(dto.getCommentReplyText())
                .user(user)
                .parentComment(parentComment)
                .build();

        commentReplyRepo.save(entity);
        log.info("댓글 저장 완료: {}", dto);
    }

    public List<CommentReplyEntity> selectAllCommentReply() {
        return commentReplyRepo.findAll();
    }

    public List<CommentReplyEntity> findByParentComment(Long commentId) {
        CommentEntity parentComment = commentRepo.findById(commentId).orElseThrow();
        return commentReplyRepo.findByParentComment(parentComment);
    }


}

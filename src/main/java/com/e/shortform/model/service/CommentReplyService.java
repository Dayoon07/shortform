package com.e.shortform.model.service;

import com.e.shortform.model.entity.CommentEntity;
import com.e.shortform.model.entity.CommentReplyEntity;
import com.e.shortform.model.entity.UserEntity;
import com.e.shortform.model.repository.CommentReplyRepo;
import com.e.shortform.model.repository.CommentRepo;
import com.e.shortform.model.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentReplyService {

    private final CommentReplyRepo commentReplyRepo;
    private final CommentRepo commentRepo;
    private final UserRepo userRepo;

    public void commentReplyInsertFuck(Long commentReplyId, String commentReplyText, Long commentReplyUserId) {
        UserEntity user = userRepo.findById(commentReplyUserId).orElseThrow();
        CommentEntity comment = commentRepo.findById(commentReplyId).orElseThrow();

        CommentReplyEntity commentReplyEntity = CommentReplyEntity.builder()
                .commentReplyText(commentReplyText)
                .user(user)
                .parentComment(comment)
                .build();

        commentReplyRepo.save(commentReplyEntity);
    }


}

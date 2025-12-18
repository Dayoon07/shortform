package com.e.shortform.domain.community.service;

import com.e.shortform.domain.comment.entity.CommentReplyEntity;
import com.e.shortform.domain.comment.repository.CommentReplyRepo;
import com.e.shortform.domain.community.entity.CommunityCommentEntity;
import com.e.shortform.domain.community.entity.CommunityCommentReplyEntity;
import com.e.shortform.domain.community.repository.CommunityCommentReplyRepo;
import com.e.shortform.domain.community.repository.CommunityCommentRepo;
import com.e.shortform.domain.community.repository.CommunityRepo;
import com.e.shortform.domain.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommunityCommentReplyService {

    private final CommunityCommentRepo communityCommentRepo;
    private final CommunityCommentReplyRepo communityCommentReplyRepo;

    public void insertCommentReply(Long id, String replyText, UserEntity user) {
        CommunityCommentEntity b = communityCommentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("찾을 수 없거나 존재하지 않는 게시글"));

        CommunityCommentReplyEntity a = CommunityCommentReplyEntity.builder()
                .replyText(replyText)
                .user(user)
                .comment(b)
                .build();
        communityCommentReplyRepo.save(a);
    }









}

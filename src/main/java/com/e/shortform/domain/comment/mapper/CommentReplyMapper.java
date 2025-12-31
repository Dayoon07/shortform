package com.e.shortform.domain.comment.mapper;

import com.e.shortform.domain.comment.res.CommentReply;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentReplyMapper {
    List<CommentReply> selectCommentReply(Long id);
}

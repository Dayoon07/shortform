package com.e.shortform.domain.comment.mapper;

import com.e.shortform.domain.comment.res.CommentButVideoRes;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<CommentButVideoRes> selectByCommentId(Long id);
    List<CommentButVideoRes> selectByCommentButOrderByIsDesc(Long id);
}

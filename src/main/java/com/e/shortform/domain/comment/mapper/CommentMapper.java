package com.e.shortform.domain.comment.mapper;

import com.e.shortform.domain.comment.res.CommentWithUserVideoDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<CommentWithUserVideoDto> selectByCommentId(Long id);
    List<CommentWithUserVideoDto> selectByCommentButOrderByIsDesc(Long id);
}

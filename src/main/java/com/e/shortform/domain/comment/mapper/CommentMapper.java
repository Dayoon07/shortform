package com.e.shortform.model.mapper;

import com.e.shortform.model.dto.CommentWithUserVideoDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<CommentWithUserVideoDto> selectByCommentId(Long id);
    List<CommentWithUserVideoDto> selectByCommentButOrderByIsDesc(Long id);
}

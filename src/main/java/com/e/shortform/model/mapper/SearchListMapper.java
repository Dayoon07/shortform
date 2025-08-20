package com.e.shortform.model.mapper;

import com.e.shortform.model.vo.SearchListVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SearchListMapper {
    List<SearchListVo> selectMySearchList(Long id);
}

package com.e.shortform.model.mapper;

import com.e.shortform.model.dto.IndexPageAllVideosDto;
import com.e.shortform.model.dto.VideoWithUserDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VideoMapper {
    VideoWithUserDto selectByVideo(String mention);
    List<IndexPageAllVideosDto> selectIndexPageAllVideos();
}

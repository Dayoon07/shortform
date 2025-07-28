package com.e.shortform.model.mapper;

import com.e.shortform.model.dto.IndexPageAllVideosDto;
import com.e.shortform.model.dto.VideoWithUserDto;
import com.e.shortform.model.dto.VideoWithUserFuckingDto;
import com.e.shortform.model.entity.VideoEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VideoMapper {
    VideoWithUserDto selectByVideo(String mention);
    List<IndexPageAllVideosDto> selectIndexPageAllVideos();
    List<IndexPageAllVideosDto> selectUserProfilePageAllVideos(String mention);
}

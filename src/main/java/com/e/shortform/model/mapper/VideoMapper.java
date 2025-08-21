package com.e.shortform.model.mapper;

import com.e.shortform.model.dto.IndexPageAllVideosDto;
import com.e.shortform.model.dto.VideoWithUserDto;
import com.e.shortform.model.entity.VideoEntity;
import com.e.shortform.model.vo.VideoVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoMapper {
    VideoWithUserDto selectByVideo(String mention);
    List<IndexPageAllVideosDto> selectIndexPageAllVideos();
    List<IndexPageAllVideosDto> selectUserProfilePageAllVideos(String mention);
    List<IndexPageAllVideosDto> searchLogic(String searchWordParam);
    VideoVo selectRandomVideo(@Param("excludeIds") List<Long> excludeIds);
    List<IndexPageAllVideosDto> myLikeVideos(Long id);
}

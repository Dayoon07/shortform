package com.e.shortform.domain.video.mapper;

import com.e.shortform.domain.video.res.IndexPageAllVideosDto;
import com.e.shortform.domain.video.res.VideoWithUserDto;
import com.e.shortform.domain.video.vo.VideoVo;
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
    VideoVo getSwipeVideo(@Param("videoLoc") String videoLoc);
    List<IndexPageAllVideosDto> myLikeVideos(Long id);
    List<IndexPageAllVideosDto> selectExploreVideoListByTags(String hashtag);
    List<IndexPageAllVideosDto> selectExploreVideoListByTagsButVideoViewsDescFuck(String hashtag);
    /** 페이징된 비디오 목록 조회 (신규) */
    List<IndexPageAllVideosDto> selectIndexPageAllVideosPaginated(@Param("offset") int offset, @Param("size") int size);
    /** public 비디오 총 개수 조회 (신규) */
    int countPublicVideos();
}

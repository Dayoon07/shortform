package com.e.shortform.model.mapper;

import com.e.shortform.model.dto.CommunityWithUserProfileDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommunityMapper {

    List<CommunityWithUserProfileDto> selectByCommunityButWhereId(Long id);
    List<CommunityWithUserProfileDto> findByCommunityBoardFuck(String uuid);

}

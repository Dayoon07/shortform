package com.e.shortform.model.mapper;

import com.e.shortform.model.dto.CommunityWithUserProfileDto;
import com.e.shortform.model.dto.UserProfilePostAllLikeCntDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommunityMapper {

    List<CommunityWithUserProfileDto> selectByCommunityButWhereId(Long id);
    UserProfilePostAllLikeCntDto findByCommunityBoardFuck(String uuid);
    List<UserProfilePostAllLikeCntDto> selectByCommunityButWhereIdAsdf(Long id);

}

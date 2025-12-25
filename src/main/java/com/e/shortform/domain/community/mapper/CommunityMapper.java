package com.e.shortform.domain.community.mapper;

import com.e.shortform.domain.community.res.CommunityWithUserProfileDto;
import com.e.shortform.domain.community.res.UserProfilePostAllLikeCntDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommunityMapper {

    List<CommunityWithUserProfileDto> selectByCommunityButWhereId(Long id);
    UserProfilePostAllLikeCntDto findByCommunityBoardF(String uuid);
    List<UserProfilePostAllLikeCntDto> selectByCommunityButWhereIdAsdf(Long id);
    boolean changeDeleteStatus(Long id);

}

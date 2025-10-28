package com.e.shortform.domain.community.mapper;

import com.e.shortform.model.dto.UserProfilePostAllLikeCntDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommunityLikeMapper {
    UserProfilePostAllLikeCntDto findByUserProfilePostAllLikeCnt(String mention);
}

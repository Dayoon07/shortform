package com.e.shortform.model.mapper;

import com.e.shortform.model.dto.UserProfilePostAllLikeCntDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommunityLikeMapper {
    UserProfilePostAllLikeCntDto findByUserProfilePostAllLikeCnt(String mention);
}

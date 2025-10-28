package com.e.shortform.domain.user.mapper;

import com.e.shortform.domain.user.res.UserProfileDto;
import com.e.shortform.domain.user.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    List<UserVo> selectAll();
    String selectChkUsername(String username);
    String selectChkUserMail(String mail);
    UserProfileDto getUserProfilePageInfo(Long id);
    List<UserVo> selectProfileUserFollowList(Long id);
    List<UserVo> selectProfileUserFollowingList(Long id);
    void updateUserInfo(@Param("username") String username, @Param("mail") String mail,
                        @Param("mention") String mention, @Param("bio") String bio,
                        @Param("profileImg") String profileImg,
                        @Param("profileImgSrc") String profileImgSrc,
                        @Param("id") Long id);

}

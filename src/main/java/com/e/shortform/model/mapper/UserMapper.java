package com.e.shortform.model.mapper;

import com.e.shortform.model.vo.UserVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

    List<UserVo> selectAll();
    String selectChkUsername(String username);
    String selectChkUserMail(String mail);

}

package com.e.shortform.model.dto;

import com.e.shortform.model.vo.UserVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchListWithUserDto {

    private Long id;
    private Long searchedUserId;
    private String searchedWord;
    private LocalDateTime createAt;

    private UserVo userVo;

}

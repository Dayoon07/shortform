package com.e.shortform.domain.search.res;

import com.e.shortform.domain.user.vo.UserVo;
import lombok.*;

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
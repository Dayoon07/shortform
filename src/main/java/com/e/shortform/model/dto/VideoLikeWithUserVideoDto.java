package com.e.shortform.model.dto;

import com.e.shortform.model.vo.UserVo;
import com.e.shortform.model.vo.VideoVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoLikeWithUserVideoDto {

    private Long id;
    private Long likeVideoId;
    private Long likerUserId;
    private LocalDateTime likeAt;

    private UserVo userVo;
    private VideoVo videoVo;

}

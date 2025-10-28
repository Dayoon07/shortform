package com.e.shortform.domain.comment.res;

import com.e.shortform.domain.user.vo.UserVo;
import com.e.shortform.domain.video.vo.VideoVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentWithUserVideoDto {

    private Long id;
    private String commentText;
    private Long commentUserId;
    private Long commentVideoId;
    private LocalDateTime createAt;

    private UserVo userVo;
    private VideoVo videoVo;

}

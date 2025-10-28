package com.e.shortform.domain.comment.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentUserFindWhatTheFuckDto {

    private Long id;
    private String commentText;
    private Long commentUserId;
    private Long commentVideoId;
    private LocalDateTime createAt;
    private String username;
    private String profileImgSrc;
    private String mention;
    private Long likeCount;

}

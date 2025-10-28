package com.e.shortform.domain.community.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityAdditionVo {

    private Long id;
    private String fileSrc;
    private Long communityId;
    private LocalDateTime uploadAt;

}

package com.e.shortform.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityVo {

    private Long id;
    private String communityText;
    private Long communityWriterId;
    private String communityUuid;
    private String communityAvailability;
    private LocalDateTime createAt;

}

package com.e.shortform.domain.video.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoRequestDto {
    private List<Long> excludeIds;
}

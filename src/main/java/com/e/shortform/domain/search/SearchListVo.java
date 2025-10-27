package com.e.shortform.model.vo;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchListVo {

    private Long id;
    private Long searchedUserId;
    private String searchedWord;
    private LocalDateTime createAt;

}

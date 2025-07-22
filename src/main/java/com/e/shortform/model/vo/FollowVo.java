package com.e.shortform.model.vo;

import com.e.shortform.model.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowVo {

    private Long id;
    private Long followUserId;
    private Long followingUserId;
    private LocalDateTime createAt;

}

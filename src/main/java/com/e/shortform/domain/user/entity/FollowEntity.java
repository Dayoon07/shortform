package com.e.shortform.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHORTFORM_USERS_FOLLOWS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 팔로우 당한 유저
    @ManyToOne
    @JoinColumn(name = "follow_user_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_FOLLOW_USER"))
    private UserEntity followUser;

    // 팔로우 한 유저
    @ManyToOne
    @JoinColumn(name = "followed_user_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_FOLLOWED_USER"))
    private UserEntity followedUser;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;

}

package com.e.shortform.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    @JoinColumn(name = "following_user_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_FOLLOWING_USER"))
    private UserEntity followingUser;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;

}

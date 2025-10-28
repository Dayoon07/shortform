package com.e.shortform.domain.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHORTFORM_COMMUNITY_LIKES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "COMMUNITY_ID",
        foreignKey = @ForeignKey(name = "FK_COMMUNITY_LIKE_COMMUNITY")
    )
    private CommunityEntity community;

    @ManyToOne
    @JoinColumn(name = "LIKER_USER_ID",
        foreignKey = @ForeignKey(name = "FK_COMMUNITY_LIKE_USER")
    )
    private UserEntity user;

    @CreationTimestamp
    @Column(name = "LIKE_AT", updatable = false)
    private LocalDateTime likeAt;

}

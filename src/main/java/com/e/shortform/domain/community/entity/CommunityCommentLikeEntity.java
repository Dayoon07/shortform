package com.e.shortform.domain.community.entity;

import com.e.shortform.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHORTFORM_COMMUNITY_COMMENT_LIKES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityCommentLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "LIKER_USER_ID",
            foreignKey = @ForeignKey(name = "FK_COMMUNITY_COMMENT_LIKE_USER")
    )
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "COMMENT_ID",
        foreignKey = @ForeignKey(name = "FK_COMMUNITY_COMMENT_LIKE_COMMENT")
    )
    private CommunityCommentEntity communityComment;

    @Column(name = "LIKE_AT", updatable = false)
    private LocalDateTime likeAt;


}

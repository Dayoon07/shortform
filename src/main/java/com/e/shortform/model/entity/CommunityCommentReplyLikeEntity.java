package com.e.shortform.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHORTFORM_COMMUNITY_COMMENT_REPLY_LIKES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityCommentReplyLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "REPLY_ID",
        foreignKey = @ForeignKey(name = "FK_COMMUNITY_REPLY_LIKE_REPLY")
    )
    private CommunityCommentReplyEntity commentReply;

    @ManyToOne
    @JoinColumn(name = "LIKER_USER_ID",
        foreignKey = @ForeignKey(name = "FK_COMMUNITY_REPLY_LIKE_USER")
    )
    private UserEntity user;

    @CreationTimestamp
    @Column(name = "LIKE_AT")
    private LocalDateTime likeAt;

}

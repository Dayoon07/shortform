package com.e.shortform.domain.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHORTFORM_COMMUNITY_COMMENT_REPLYS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityCommentReplyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Lob
    @Column(name = "REPLY_TEXT", nullable = false)
    private String replyText;

    @ManyToOne
    @JoinColumn(name = "REPLY_USER_ID",
        foreignKey = @ForeignKey(name = "FK_COMMUNITY_REPLY_USER")
    )
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "COMMENT_ID",
        foreignKey = @ForeignKey(name = "FK_COMMUNITY_REPLY_COMMENT")
    )
    private CommunityCommentEntity comment;

    @Column(name = "CREATE_AT", updatable = false)
    private LocalDateTime createAt;

}

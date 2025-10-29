package com.e.shortform.domain.community.entity;

import com.e.shortform.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHORTFORM_COMMUNITY_COMMENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityCommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "COMMENT_TEXT", nullable = false)
    private String commentText;

    @ManyToOne
    @JoinColumn(name = "COMMENT_USER_ID",
        foreignKey = @ForeignKey(name = "FK_COMMUNITY_COMMENT_USER")
    )
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "COMMUNITY_ID",
        foreignKey = @ForeignKey(name = "FK_COMMUNITY_COMMENT_COMMUNITY")
    )
    private CommunityEntity community;

    @CreationTimestamp
    @Column(name = "CREATE_AT", updatable = false)
    private LocalDateTime createAt;

}

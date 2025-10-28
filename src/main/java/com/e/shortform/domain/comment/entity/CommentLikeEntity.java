package com.e.shortform.domain.comment.entity;

import com.e.shortform.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHORTFORM_COMMENT_LIKES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_COMMENT_LIKE_COMMENT"))
    private CommentEntity comment;

    @ManyToOne
    @JoinColumn(name = "liker_user_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_COMMENT_LIKE_USER"))
    private UserEntity user;

    @CreationTimestamp
    @Column(name = "like_at", updatable = false)
    private LocalDateTime likeAt;

}

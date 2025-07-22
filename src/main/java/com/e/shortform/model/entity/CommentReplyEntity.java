package com.e.shortform.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHORTFORM_COMMENT_REPLYS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentReplyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "comment_reply_text", nullable = false)
    private String commentReplyText;

    @ManyToOne
    @JoinColumn(name = "comment_reply_user_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_REPLY_USER"))
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "comment_reply_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_REPLY_COMMENT"))
    private CommentEntity parentComment;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;

}

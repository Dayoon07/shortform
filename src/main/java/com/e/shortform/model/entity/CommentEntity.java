package com.e.shortform.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHORTFORM_COMMENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "comment_text", nullable = false)
    private String commentText;

    @ManyToOne
    @JoinColumn(name = "comment_user_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_COMMENT_USER"))
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "comment_video_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_COMMENT_VIDEO"))
    private VideoEntity video;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;

}


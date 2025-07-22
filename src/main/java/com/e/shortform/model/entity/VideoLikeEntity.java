package com.e.shortform.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHORTFORM_VIDEO_LIKES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "like_video_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_LIKE_VIDEO"))
    private VideoEntity video;

    @ManyToOne
    @JoinColumn(name = "liker_user_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_LIKER_USER"))
    private UserEntity user;

    @CreationTimestamp
    @Column(name = "like_at", updatable = false)
    private LocalDateTime likeAt;

}

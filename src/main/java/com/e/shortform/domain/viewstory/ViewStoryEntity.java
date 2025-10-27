package com.e.shortform.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHORTFORM_VIEWSTORYS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewStoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "watched_video_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_WATCHED_VIDEO"))
    private VideoEntity video;

    @ManyToOne
    @JoinColumn(name = "watched_user_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_WATCHED_USER"))
    private UserEntity user;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;

}

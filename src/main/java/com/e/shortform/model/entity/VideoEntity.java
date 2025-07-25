package com.e.shortform.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHORTFORM_VIDEOS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "video_title", nullable = false)
    private String videoTitle;

    @Lob
    @Column(name = "video_description")
    private String videoDescription;

    @Lob
    @Column(name = "video_name", nullable = false)
    private String videoName;

    @Lob
    @Column(name = "video_src", nullable = false)
    private String videoSrc;

    @Column(name = "video_tag")
    private String videoTag;

    @Column(name = "video_views")
    private Long videoViews;

    @Column(name = "video_loc", nullable = false, unique = true)
    private String videoLoc;

    @ManyToOne
    @JoinColumn(name = "uploader_user_id", nullable = false,
            foreignKey = @ForeignKey(name = "FK_UPLOADER"))
    private UserEntity uploader;

    @Column(name = "video_watch_availability", nullable = false)
    private String videoWatchAvailability;

    @Column(name = "comment_availability", nullable = false)
    private Integer commentAvailability;

    @CreationTimestamp
    @Column(name = "upload_at", updatable = false)
    private LocalDateTime uploadAt;

}

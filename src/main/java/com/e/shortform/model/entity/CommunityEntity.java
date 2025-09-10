package com.e.shortform.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHORTFORM_COMMUNITYS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Lob
    @Column(name = "COMMUNITY_TEXT")
    private String communityText;

    @ManyToOne
    @JoinColumn(name = "COMMUNITY_WRITER_ID",
            foreignKey = @ForeignKey(name = "FK_COMMUNITY_WRITER")
    )
    private UserEntity user;

    @Column(name = "COMMUNITY_UUID", nullable = false, unique = true)
    private String communityUuid;

    @Column(name = "COMMUNITY_AVAILABILITY", nullable = false)
    private String communityAvailability;

    @CreationTimestamp
    @Column(name = "CREATE_AT", updatable = false)
    private LocalDateTime createAt;

}

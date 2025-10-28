package com.e.shortform.domain.community.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHORTFORM_COMMUNITY_ADDITIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityAdditionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "FILE_SRC", nullable = false)
    private String fileSrc;

    @ManyToOne
    @JoinColumn(name = "COMMUNITY_ID",
        foreignKey = @ForeignKey(name = "FK_ADDITION_COMMUNITY")
    )
    private CommunityEntity community;

    @CreationTimestamp
    @Column(name = "UPLOAD_AT", updatable = false)
    private LocalDateTime uploadAt;

}

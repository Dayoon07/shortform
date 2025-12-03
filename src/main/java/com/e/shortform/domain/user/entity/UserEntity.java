package com.e.shortform.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHORTFORM_USERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "mail", nullable = false, unique = true)
    private String mail;

    @Column(name = "password", nullable = false)
    private String password;

    @Lob
    @Column(name = "profile_img", nullable = false)
    private String profileImg;

    @Lob
    @Column(name = "profile_img_src", nullable = false)
    private String profileImgSrc;

    @Lob
    @Column(name = "bio")
    private String bio;

    @Column(name = "mention", nullable = false, unique = true)
    private String mention;

    @Column(name = "create_at", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime createAt;

    @Column(name = "is_social", nullable = false)
    private boolean social;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

}

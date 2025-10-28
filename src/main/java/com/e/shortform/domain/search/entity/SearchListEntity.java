package com.e.shortform.domain.search.entity;

import com.e.shortform.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHORTFORM_SEARCH_LISTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchListEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "searched_user_id",
            foreignKey = @ForeignKey(name = "FK_SEARCHED_USER_ID"))
    private UserEntity user;

    @Column(name = "searched_word", nullable = false)
    private String searchedWord;

    @CreationTimestamp
    @Column(name = "create_at", updatable = false)
    private LocalDateTime createAt;

}

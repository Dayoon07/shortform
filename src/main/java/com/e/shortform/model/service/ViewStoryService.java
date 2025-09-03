package com.e.shortform.model.service;

import com.e.shortform.model.entity.UserEntity;
import com.e.shortform.model.entity.VideoEntity;
import com.e.shortform.model.entity.ViewStoryEntity;
import com.e.shortform.model.repository.UserRepo;
import com.e.shortform.model.repository.VideoRepo;
import com.e.shortform.model.repository.ViewStoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ViewStoryService {

    private final ViewStoryRepo viewStoryRepo;
    private final UserRepo userRepo;
    private final VideoRepo videoRepo;

    public void userViewstoryInsert(Long watchedUserId, Long watchedVideoId) {
        UserEntity watchingUser = userRepo.findById(watchedUserId).orElseThrow();
        VideoEntity watchVideo = videoRepo.findById(watchedVideoId).orElseThrow();

        ViewStoryEntity viewStoryEntity = ViewStoryEntity.builder()
                .video(watchVideo)
                .user(watchingUser)
                .build();

        viewStoryRepo.save(viewStoryEntity);
    }

    public List<ViewStoryEntity> getViewStoryListByUserId(Long userId) {
        return viewStoryRepo.findByUser(userRepo.findById(userId).orElseThrow());
    }

}

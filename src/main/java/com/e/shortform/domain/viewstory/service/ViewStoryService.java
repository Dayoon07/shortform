package com.e.shortform.domain.viewstory.service;

import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.repository.UserRepo;
import com.e.shortform.domain.video.entity.VideoEntity;
import com.e.shortform.domain.video.repository.VideoRepo;
import com.e.shortform.domain.viewstory.entity.ViewStoryEntity;
import com.e.shortform.domain.viewstory.repository.ViewStoryRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
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

    public List<ViewStoryEntity> selectAllViewStory() {
        return viewStoryRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

}

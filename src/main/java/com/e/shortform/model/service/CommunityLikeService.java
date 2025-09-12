package com.e.shortform.model.service;

import com.e.shortform.model.dto.UserProfilePostAllLikeCntDto;
import com.e.shortform.model.entity.CommunityEntity;
import com.e.shortform.model.entity.CommunityLikeEntity;
import com.e.shortform.model.entity.UserEntity;
import com.e.shortform.model.mapper.CommunityLikeMapper;
import com.e.shortform.model.repository.CommunityLikeRepo;
import com.e.shortform.model.repository.CommunityRepo;
import com.e.shortform.model.repository.UserRepo;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommunityLikeService {

    private final CommunityRepo communityRepo;
    private final CommunityLikeRepo communityLikeRepo;
    private final UserRepo userRepo;

    private final CommunityLikeMapper communityLikeMapper;

    @Transactional
    public Map<String, Object> postLike(String uuid, HttpSession session) {
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        CommunityEntity c = communityRepo.findByCommunityUuid(uuid);
        if (c == null) {
            throw new IllegalArgumentException("해당 UUID에는 게시글이 없습니다.");
        }

        Optional<CommunityLikeEntity> likeOpt = communityLikeRepo.findByUserAndCommunity(user, c);
        boolean like;

        if (likeOpt.isPresent()) {
            communityLikeRepo.delete(likeOpt.get());
            like = false;
        } else {
            CommunityLikeEntity entity = CommunityLikeEntity.builder()
                    .community(c)
                    .user(user)
                    .build();
            communityLikeRepo.save(entity);
            like = true;
        }

        // 실제 DB에서 카운트 가져오기
        long count = communityLikeRepo.countByCommunity(c);

        return Map.of(
                "message", like ? "좋아요 요청이 전달되었습니다." : "좋아요 취소 요청이 전달되었습니다",
                "like", like,
                "count", count
        );
    }

    public UserProfilePostAllLikeCntDto findByUserProfilePostAllLikeCnt(String mention) {
        return communityLikeMapper.findByUserProfilePostAllLikeCnt(mention);
    }

}

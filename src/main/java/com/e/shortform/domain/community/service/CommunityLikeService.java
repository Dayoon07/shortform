package com.e.shortform.domain.community.service;

import com.e.shortform.domain.community.entity.CommunityEntity;
import com.e.shortform.domain.community.entity.CommunityLikeEntity;
import com.e.shortform.domain.community.mapper.CommunityLikeMapper;
import com.e.shortform.domain.community.repository.CommunityLikeRepo;
import com.e.shortform.domain.community.repository.CommunityRepo;
import com.e.shortform.domain.community.res.UserProfilePostAllLikeCntDto;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.repository.UserRepo;
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
    public Map<String, Object> postLike(String uuid, String reqMention) {
        UserEntity user = userRepo.findByMention(reqMention);
        CommunityEntity c = communityRepo.findByCommunityUuid(uuid);

        if (user == null)
            throw new IllegalStateException("로그인이 필요합니다.");
        if (c == null)
            throw new IllegalArgumentException("해당 UUID에는 게시글이 없습니다.");

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

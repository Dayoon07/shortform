"use strict";

const videoWrapper = document.getElementById("video-container-fuck");
let startY = 0;

// 시청한 영상 ID들을 추적 (중복 방지용)
let watchedVideoIds = new Set();
let videoHistory = [];
let currentVideoIndex = 0;

// 페이지 로드시 현재 영상 ID를 추가
document.addEventListener('DOMContentLoaded', function() {
    const currentVideo = document.getElementById('main-video');
    if (currentVideo) {
        const currentVideoId = parseInt(currentVideo.dataset.videoId);
        watchedVideoIds.add(currentVideoId);

        // 현재 영상 데이터 수집
        const currentVideoData = collectCurrentVideoData();
        videoHistory = [currentVideoData];
        currentVideoIndex = 0;
    }
});

// 현재 페이지의 영상 데이터를 수집하는 함수
function collectCurrentVideoData() {
    const currentVideo = document.getElementById('main-video');
    const likeBtn = document.getElementById('like-btn');
    const followBtn = document.getElementById('user-follow-btn') || document.getElementById('user-following-btn');

    return {
        id: parseInt(currentVideo.dataset.videoId),
        videoSrc: currentVideo.src,
        videoLoc: currentVideo.dataset.videoLoc,
        title: document.getElementById("video-title").textContent || '',
        description: document.getElementById("video-description").textContent || '',
        videoTag: document.getElementById("main-video-info-tag-fuck")?.value || '',
        uploader: {
            id: document.getElementById("uploader-profile-img-src").dataset.userId,
            mention: document.querySelector(".uploader-mention").textContent.replace('@', ''),
            username: document.querySelector(".uploader-username").textContent || '',
            profileImgSrc: document.getElementById("uploader-profile-img-src").src
        },
        likeCount: document.getElementById("like-count")?.textContent || document.querySelector(".like-count")?.textContent || '0',
        commentCount: document.getElementById("comment-btn").nextElementSibling?.textContent || '0',
        isLiked: likeBtn ? likeBtn.dataset.isLiked === "true" : false,
        isFollowing: followBtn ? followBtn.id === 'user-following-btn' : false
    };
}

// 모바일 터치 시작
window.addEventListener("touchstart", (e) => {
    startY = e.touches[0].clientY;
});

// 모바일 터치 끝
window.addEventListener("touchend", (e) => {
    const endY = e.changedTouches[0].clientY;
    handleSwipe(startY - endY);
});

// PC 휠 이벤트
window.addEventListener("wheel", (e) => {
    handleSwipe(e.deltaY);
});

// 방향 판단 함수
function handleSwipe(delta) {
    if (delta > 50) {
        console.log("⬇️ 다음 영상");
        nextVideo();
    } else if (delta < -50) {
        console.log("⬆️ 이전 영상");
        prevVideo();
    }
}

// 다음 영상 로딩
async function nextVideo() {
    try {
        showLoadingIndicator();

        // 현재 히스토리에서 다음 영상이 있는지 확인
        if (currentVideoIndex < videoHistory.length - 1) {
            // 히스토리에서 다음 영상으로 이동
            currentVideoIndex++;
            const nextVideoData = videoHistory[currentVideoIndex];
            console.log('히스토리에서 다음 영상으로 이동:', nextVideoData);
            await transitionToVideo(nextVideoData);
            return;
        }

        // 새로운 영상을 서버에서 가져오기
        const excludeIdsArray = Array.from(watchedVideoIds);
        const response = await fetch(`${location.origin}/api/videos/random`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ excludeIds: excludeIdsArray })
        });

        if (!response.ok) throw new Error('영상을 불러올 수 없습니다.');

        const responseData = await response.json();
        console.log('서버 응답:', responseData);

        if (responseData.hasMore === false) {
            showErrorMessage('더 이상 시청할 영상이 없습니다.');
            return;
        }

        const videoData = normalizeVideoData(responseData);
        if (!videoData || !videoData.id) {
            throw new Error('유효하지 않은 영상 데이터입니다.');
        }

        if (watchedVideoIds.has(videoData.id)) {
            console.warn('중복 영상 감지, 다시 요청합니다.');
            nextVideo();
            return;
        }

        watchedVideoIds.add(videoData.id);

        // 메모리 관리
        if (watchedVideoIds.size > 1000) {
            const oldIds = Array.from(watchedVideoIds).slice(0, 500);
            oldIds.forEach(id => watchedVideoIds.delete(id));
        }

        // 히스토리에 새 영상 추가
        videoHistory.push(videoData);
        currentVideoIndex = videoHistory.length - 1;

        await transitionToVideo(videoData);

    } catch (error) {
        console.error('다음 영상 로딩 실패:', error);
        showErrorMessage('다음 영상을 불러올 수 없습니다.');
    } finally {
        hideLoadingIndicator();
    }
}

// 서버 응답 데이터를 정규화하는 함수
function normalizeVideoData(responseData) {
    try {
        const video = responseData.video;
        if (!video) {
            console.error('video 필드가 없습니다:', responseData);
            return null;
        }

        return {
            id: video.id,
            videoSrc: video.videoSrc,
            videoLoc: video.videoLoc || video.id,
            title: video.videoTitle || '',
            description: video.videoDescription || '',
            videoTag: video.videoTag || '',
            uploader: {
                id: video.uploader?.id,
                mention: video.uploader?.mention || '',
                username: video.uploader?.username || '',
                profileImgSrc: video.uploader?.profileImgSrc || '/images/default-profile.png'
            },
            likeCount: responseData.likeCnt || 0,
            commentCount: responseData.commentCnt || 0,
            isLiked: responseData.isLiked || false,
            isFollowing: responseData.isFollowing || false
        };
    } catch (error) {
        console.error('데이터 정규화 중 오류:', error);
        return null;
    }
}

// 이전 영상으로 돌아가기
async function prevVideo() {
    if (currentVideoIndex <= 0) {
        showErrorMessage('첫 번째 영상입니다.');
        return;
    }

    try {
        showLoadingIndicator();
        currentVideoIndex--;
        const prevVideoData = videoHistory[currentVideoIndex];
        console.log('이전 영상으로 이동:', prevVideoData);
        await transitionToVideo(prevVideoData);
    } catch (error) {
        console.error('이전 영상 로딩 실패:', error);
        showErrorMessage('이전 영상을 불러올 수 없습니다.');
        currentVideoIndex++;
    } finally {
        hideLoadingIndicator();
    }
}

// 팔로우 버튼 업데이트 함수
function updateFollowButton(videoData) {
    const followContainer = document.getElementById("follow-button-container");
    if (!followContainer) return;

    const realFollowBtn = document.querySelector("#follow-button-container > *");
    const user = JSON.parse(localStorage.getItem("user"));

    if (user.id !== videoData.uploader.id) {
        realFollowBtn.type = "button";
        realFollowBtn.dataset.mention = videoData.uploader.mention;
        realFollowBtn.id = videoData.isFollowing === true ? "user-following-btn" : "user-follow-btn";
        realFollowBtn.className = videoData.isFollowing === true
            ? "bg-gray-600 hover:bg-red-600 px-4 md:px-4 py-2 rounded-md transition-colors duration-200 flex items-center space-x-2 text-xs md:text-sm"
            : "bg-gradient-to-r from-pink-500 to-sky-500 hover:from-pink-600 hover:to-sky-600 px-4 md:px-4 py-2 rounded-md transition-all duration-200 flex items-center space-x-2 text-xs md:text-sm"
        realFollowBtn.classList.remove("hidden");
    } else {
        console.log("업로더와 현재 사용자가 같습니다. 자기 자신은 팔로우 할 수 없습니다.");
        realFollowBtn.classList.add("hidden");
    }

    if (videoData.isFollowing === true) {
        realFollowBtn.innerHTML = `
            <span>팔로잉</span>
            <svg class="w-3 h-3 md:w-4 md:h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
            </svg>
        `;
    } else {
        realFollowBtn.innerHTML = `
            <span>팔로우</span>
            <svg class="w-3 h-3 md:w-4 md:h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
            </svg>
        `;
    }

    // 새 버튼에 이벤트 리스너 추가 (user-follow.js의 이벤트와 연동)
    if (window.attachFollowListener) {
        window.attachFollowListener(realFollowBtn);
    }
}

// 부드러운 페이지 전환
async function transitionToVideo(videoData) {
    try {
        console.log('전환할 영상 데이터:', videoData);

        const mainVideo = document.getElementById('main-video');
        if (!mainVideo) return;

        if (!videoData.videoSrc || !videoData.uploader) {
            throw new Error('필수 영상 데이터가 누락되었습니다.');
        }

        mainVideo.style.transition = 'opacity 0.3s ease-in-out';
        mainVideo.style.opacity = '0';

        await new Promise(resolve => setTimeout(resolve, 300));

        mainVideo.src = videoData.videoSrc;
        mainVideo.dataset.videoId = videoData.id;
        mainVideo.dataset.videoLoc = videoData.videoLoc;
        mainVideo.load();

        mainVideo.onloadeddata = () => {
            mainVideo.play().catch(e => console.log('자동재생 실패:', e));
        };

        // 업로더 정보 업데이트
        document.getElementById("uploader-profile-img-src").src = videoData.uploader.profileImgSrc;
        document.getElementById("uploader-profile-img-src").dataset.userId = videoData.uploader.id;

        const usernameElements = document.querySelectorAll("#uploader-profile-data-info a");
        usernameElements.forEach(link => {
            if (videoData.uploader && videoData.uploader.mention) {
                link.href = `${location.origin}/@${videoData.uploader.mention}`;
            }
        });

        const usernameSpans = document.querySelectorAll(".uploader-username");
        usernameSpans.forEach(span => {
            if (videoData.uploader && videoData.uploader.username) {
                const username = videoData.uploader.username;
                span.textContent = username.length > 10 ? username.substring(0, 10) + '...' : username;
            }
        });

        const mentionSpans = document.querySelectorAll(".uploader-mention");
        mentionSpans.forEach(span => {
            if (videoData.uploader && videoData.uploader.mention) {
                const mention = videoData.uploader.mention;
                span.textContent = mention.length > 10 ? `@${mention.substring(0, 10)}...` : `@${mention}`;
            }
        });

        // 영상 정보 업데이트
        document.getElementById("video-title").textContent = videoData.title;
        const descriptionElements = document.querySelectorAll('.text-gray-300.line-clamp-2');
        descriptionElements.forEach(el => {
            if (videoData.description) {
                el.textContent = videoData.description;
            }
        });

        // 태그 업데이트
        const tagContainer = document.getElementById("video-tags");
        if (tagContainer) {
            tagContainer.innerHTML = '';
            if (videoData.videoTag) {
                const tags = videoData.videoTag.split(' ');
                if (tags.length >= 1) {
                    for (let i = 0; i < tags.length; i++) {
                        const tagElement = document.createElement("a");
                        tagElement.href = `${location.origin}/hashtag/${tags[i].trim()}`;
                        tagElement.textContent = tags[i].trim();
                        tagElement.className = 'text-xs bg-white bg-opacity-10 text-blue-300 px-2 py-1 rounded-full cursor-pointer hover:bg-opacity-20 transition-all duration-200';
                        tagContainer.appendChild(tagElement);
                    }
                }
            }
        } else {
            const wtf = document.getElementById("main-video-tags-what");

            const $tagCon = document.createElement("div");
            $tagCon.id = "video-tags";
            $tagCon.className = "flex flex-wrap gap-1 md:gap-2";

            const t = videoData.videoTag.split(' ');
            if (t.length >= 1) {
                for (let j = 0; j < t.length; j++) {
                    const $iDonTKnowThis = document.createElement("a");
                    $iDonTKnowThis.href = `${location.origin}/hashtag/${t[j].trim()}`;
                    $iDonTKnowThis.textContent = t[j].trim();
                    $iDonTKnowThis.className = 'text-xs bg-white bg-opacity-10 text-blue-300 px-2 py-1 rounded-full cursor-pointer hover:bg-opacity-20 transition-all duration-200 video-tag';
                    $tagCon.appendChild($iDonTKnowThis);
                }
            }

            wtf.appendChild($tagCon);
        }

        const hiddenTagInput = document.getElementById("main-video-info-tag-fuck");
        if (hiddenTagInput) {
            hiddenTagInput.value = videoData.videoTag || '';
        }

        const commentModalTitle = document.getElementById("comment-modal-title");
        if (commentModalTitle) {
            commentModalTitle.textContent = `댓글 ${videoData.commentCount}개`;
        }

        // 상호작용 요소 업데이트
        const likeBtn = document.getElementById("like-btn");
        if (likeBtn) {
            likeBtn.dataset.videoId = videoData.id;
            likeBtn.dataset.isLiked = videoData.isLiked;

            const heartIcon = document.getElementById("like-btn-svg");
            if (videoData.isLiked === true) {
                heartIcon.style.fill = "rgb(239, 68, 68)";
                heartIcon.style.stroke = "rgb(239, 68, 68)";
                heartIcon.style.color = "rgb(239, 68, 68)";
            } else {
                heartIcon.style.fill = "";
                heartIcon.style.stroke = "";
                heartIcon.style.color = "";
            }
        }

        const likeCountElement = document.getElementById("like-count") || document.querySelector(".like-count");
        if (likeCountElement) {
            likeCountElement.textContent = videoData.likeCount || '0';
        }

        const commentBtn = document.querySelector("#comment-btn");
        if (commentBtn && commentBtn.nextElementSibling) {
            commentBtn.nextElementSibling.textContent = videoData.commentCount || '0';
        }

        const commentModalHeader = document.querySelector('#video-comment-size');
        if (commentModalHeader) {
            commentModalHeader.textContent = `댓글 ${videoData.commentCount || 0}개`;
        }

        const commentList = document.querySelector('#comment-list-wawawawawa');
        if (commentList) {
            commentList.dataset.videoId = videoData.id;
            commentList.innerHTML = '';
        }

        const commentTextarea = document.querySelector('#commentText');
        if (commentTextarea) {
            commentTextarea.dataset.videoId = videoData.id;
            commentTextarea.value = '';
        }

        // 팔로우 버튼 업데이트
        updateFollowButton(videoData);

        // 페이지 제목 및 URL 업데이트
        if (videoData.title) {
            document.title = `${videoData.title} | FlipFlop`;
        }

        const newUrl = `/@${videoData.uploader.mention}/swipe/video/${videoData.videoLoc || videoData.id}`;
        history.pushState({ videoId: videoData.id }, '', newUrl);

        mainVideo.style.opacity = '1';
        console.log('영상 전환 완료:', videoData);

    } catch (error) {
        console.error('영상 전환 실패:', error);
        throw error;
    }
}

// 로딩 인디케이터 표시
function showLoadingIndicator() {
    const existingLoader = document.getElementById('video-loading');
    if (existingLoader) return;

    const loader = document.createElement('div');
    loader.id = 'video-loading';
    loader.className = 'fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50';
    loader.innerHTML = `
        <div class="flex flex-col items-center space-y-4">
            <div class="animate-spin rounded-full h-12 w-12 border-b-2 border-pink-500"></div>
            <p class="text-white text-sm">영상 로딩중...</p>
        </div>
    `;
    document.body.appendChild(loader);
}

// 로딩 인디케이터 숨김
function hideLoadingIndicator() {
    const loader = document.getElementById('video-loading');
    if (loader) loader.remove();
}

// 에러 메시지 표시
function showErrorMessage(message) {
    const existingError = document.querySelector('.error-message');
    if (existingError) existingError.remove();

    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message fixed top-20 left-1/2 transform -translate-x-1/2 bg-red-500 text-white px-6 py-3 rounded-lg shadow-lg z-50';
    errorDiv.textContent = message;

    document.body.appendChild(errorDiv);

    setTimeout(() => {
        if (errorDiv.parentNode) errorDiv.remove();
    }, 3000);
}

// 키보드 단축키 지원
document.addEventListener('keydown', function(e) {
    const tagName = e.target.tagName.toLowerCase();
    const isTyping = tagName === 'input' || tagName === 'textarea' || e.target.isContentEditable;
    if (isTyping) return;

    switch(e.key) {
        case 'ArrowDown': e.preventDefault(); nextVideo(); break;
        case 'ArrowUp': e.preventDefault(); prevVideo(); break;
        case ' ':
            e.preventDefault();
            const video = document.getElementById('main-video');
            if (video) video.paused ? video.play() : video.pause();
            break;
    }
});

// 디버깅용
window.debugVideoHistory = function() {
    console.log('현재 히스토리:', videoHistory);
    console.log('현재 인덱스:', currentVideoIndex);
    console.log('시청한 영상 개수:', watchedVideoIds.size);
};

window.addEventListener('beforeunload', function() {
    hideLoadingIndicator();
});
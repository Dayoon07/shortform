"use strict";

const videoWrapper = document.getElementById("video-container-fuck");
let startY = 0;

// 시청한 영상 ID들을 추적 (중복 방지용)
let watchedVideoIds = new Set(); // Array 대신 Set 사용으로 중복 체크 성능 향상
let videoHistory = []; // 이전 영상으로 돌아가기용
let currentVideoIndex = 0;

// 페이지 로드시 현재 영상 ID를 추가
document.addEventListener('DOMContentLoaded', function() {
    const currentVideo = document.getElementById('main-video');
    if (currentVideo) {
        const currentVideoId = parseInt(currentVideo.dataset.videoId);
        watchedVideoIds.add(currentVideoId);

        // 현재 영상을 히스토리의 첫 번째 항목으로 설정
        videoHistory = [{
            id: currentVideoId,
            url: window.location.pathname,
            videoData: {
                id: currentVideoId,
                videoSrc: currentVideo.src,
                title: document.querySelector(".text-white.font-medium")?.textContent || "",
                videoLoc: window.location.pathname,
                uploader: {
                    mention: "",
                    username: document.querySelector(".text-sm.font-semibold")?.textContent || ""
                }
            }
        }];
        currentVideoIndex = 0;
    }
});

// 모바일 터치 시작
videoWrapper.addEventListener("touchstart", (e) => {
    startY = e.touches[0].clientY;
});

// 모바일 터치 끝
videoWrapper.addEventListener("touchend", (e) => {
    const endY = e.changedTouches[0].clientY;
    handleSwipe(startY - endY);
});

// PC 휠 이벤트
videoWrapper.addEventListener("wheel", (e) => {
    e.preventDefault(); // 기본 스크롤 방지
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

        const excludeIdsArray = Array.from(watchedVideoIds);

        const response = await fetch('/api/videos/random', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ excludeIds: excludeIdsArray })
        });

        if (!response.ok) throw new Error('영상을 불러올 수 없습니다.');

        const videoData = await response.json();

        if (videoData.hasMore === false) {
            showErrorMessage('더 이상 시청할 영상이 없습니다.');
            return;
        }

        if (watchedVideoIds.has(videoData.id)) {
            console.warn('중복 영상 감지, 다시 요청합니다.');
            nextVideo();
            return;
        }

        watchedVideoIds.add(videoData.id);

        if (watchedVideoIds.size > 1000) {
            const oldIds = Array.from(watchedVideoIds).slice(0, 500);
            oldIds.forEach(id => watchedVideoIds.delete(id));
        }

        videoHistory = videoHistory.slice(0, currentVideoIndex + 1);
        videoHistory.push({ id: videoData.id, url: `/@${videoData.uploader.mention}/swipe/video/${videoData.videoLoc}`, videoData });
        currentVideoIndex = videoHistory.length - 1;

        await transitionToVideo(videoData);

    } catch (error) {
        console.error('다음 영상 로딩 실패:', error);
        showErrorMessage('다음 영상을 불러올 수 없습니다.');
    } finally {
        hideLoadingIndicator();
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
        const prevVideoData = videoHistory[currentVideoIndex].videoData;

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

// 부드러운 페이지 전환 (SPA 스타일) - 모든 UI 요소 업데이트
async function transitionToVideo(videoData) {
    try {
        const mainVideo = document.getElementById('main-video');
        if (!mainVideo) return;

        // 페이드 아웃 효과
        mainVideo.style.transition = 'opacity 0.3s ease-in-out';
        mainVideo.style.opacity = '0';

        await new Promise(resolve => setTimeout(resolve, 300));

        // 영상 소스 변경
        mainVideo.src = videoData.videoSrc;
        mainVideo.dataset.videoId = videoData.id;
        mainVideo.load();
        mainVideo.play();

        // === 업로더 정보 업데이트 ===
        // 프로필 이미지들 업데이트
        const profileImages = document.querySelectorAll('img[alt="프로필"]');
        profileImages.forEach(img => {
            if (videoData.uploader.profileImgSrc) {
                img.src = videoData.uploader.profileImgSrc;
            }
        });

        // 업로더 이름과 링크 업데이트
        const usernameElements = document.querySelectorAll('a[href*="/@"]');
        usernameElements.forEach(link => {
            if (videoData.uploader.mention) {
                link.href = `/@${videoData.uploader.mention}`;
            }
        });

        // 사용자 이름 텍스트 업데이트
        const usernameSpans = document.querySelectorAll('.text-sm.font-semibold span, .text-lg.font-semibold span');
        usernameSpans.forEach(span => {
            if (videoData.uploader.username) {
                const username = videoData.uploader.username;
                span.textContent = username.length > 10 ? username.substring(0, 10) + '...' : username;
            }
        });

        // 멘션 텍스트 업데이트
        const mentionSpans = document.querySelectorAll('.text-gray-300 span');
        mentionSpans.forEach(span => {
            if (videoData.uploader.mention) {
                const mention = videoData.uploader.mention;
                span.textContent = mention.length > 10 ? `@${mention.substring(0, 10)}...` : `@${mention}`;
            }
        });

        // === 영상 정보 업데이트 ===
        // 제목 업데이트
        const titleElements = document.querySelectorAll('.text-white.font-medium');
        titleElements.forEach(el => {
            if (videoData.title) {
                el.textContent = videoData.title;
            }
        });

        // 설명 업데이트
        const descriptionElements = document.querySelectorAll('.text-gray-300.line-clamp-2');
        descriptionElements.forEach(el => {
            if (videoData.description) {
                el.textContent = videoData.description;
            }
        });

        // === 태그 업데이트 ===
        const tagContainer = document.querySelector('.flex.flex-wrap.gap-1');
        if (tagContainer && videoData.videoTag) {
            tagContainer.innerHTML = '';
            const tags = videoData.videoTag.split(' ').filter(tag => tag.trim());
            tags.forEach(tag => {
                const tagElement = document.createElement('a');
                tagElement.href = `/hashtag/${tag}`;
                tagElement.textContent = tag;
                tagElement.className = 'text-xs bg-white bg-opacity-10 text-blue-300 px-2 py-1 rounded-full cursor-pointer hover:bg-opacity-20 transition-all duration-200';
                tagContainer.appendChild(tagElement);
            });
        } else if (tagContainer && !videoData.videoTag) {
            tagContainer.innerHTML = ''; // 태그가 없으면 비우기
        }

        // === 상호작용 요소 업데이트 ===
        // 좋아요 버튼 업데이트
        const likeBtn = document.querySelector("#like-btn");
        if (likeBtn) {
            likeBtn.dataset.videoId = videoData.id;
            likeBtn.dataset.isLiked = videoData.isLiked || 'false';

            const heartIcon = likeBtn.querySelector('svg');
            if (heartIcon) {
                if (videoData.isLiked) {
                    heartIcon.classList.remove('heart-empty');
                    heartIcon.classList.add('heart-filled');
                } else {
                    heartIcon.classList.remove('heart-filled');
                    heartIcon.classList.add('heart-empty');
                }
            }
        }

        // 좋아요 수 업데이트
        const likeCountElements = document.querySelectorAll("#like-count");
        likeCountElements.forEach(el => {
            el.textContent = videoData.likeCount || '0';
        });

        // 댓글 수 업데이트
        const commentCountElements = document.querySelectorAll('span');
        commentCountElements.forEach(el => {
            if (el.textContent === '댓글' || el.textContent.includes('댓글')) {
                el.textContent = videoData.commentCount || '0';
            }
        });

        // 댓글 모달 헤더의 댓글 수 업데이트
        const commentModalHeader = document.querySelector('#video-comment-size');
        if (commentModalHeader) {
            commentModalHeader.textContent = `댓글 ${videoData.commentCount || 0}개`;
        }

        // 댓글 모달의 댓글 목록 초기화
        const commentList = document.querySelector('#comment-list-wawawawawa');
        if (commentList) {
            commentList.dataset.videoId = videoData.id;
            commentList.innerHTML = ''; // 이전 댓글들 제거
        }

        // 댓글 텍스트 영역 업데이트
        const commentTextarea = document.querySelector('#commentText');
        if (commentTextarea) {
            commentTextarea.dataset.videoId = videoData.id;
            commentTextarea.value = ''; // 텍스트 영역 초기화
        }

        // 페이지 제목 업데이트
        if (videoData.title) {
            document.title = `${videoData.title} | FlipFlop`;
        }

        // URL 업데이트
        const newUrl = `/@${videoData.uploader.mention}/swipe/video/${videoData.videoLoc}`;
        history.pushState({ videoId: videoData.id }, '', newUrl);

        // 페이드 인 효과
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
        case ' ': // 스페이스바
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
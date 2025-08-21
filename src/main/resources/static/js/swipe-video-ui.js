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
            videoData: null // 현재 페이지는 이미 로드되어 있으므로 null
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

        // Set을 Array로 변환해서 전송
        const excludeIdsArray = Array.from(watchedVideoIds);

        const response = await fetch('/api/videos/random', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                excludeIds: excludeIdsArray
            })
        });

        if (!response.ok) {
            throw new Error('영상을 불러올 수 없습니다.');
        }

        const videoData = await response.json();

        // 더 이상 영상이 없는 경우 처리
        if (videoData.hasMore === false) {
            showErrorMessage('더 이상 시청할 영상이 없습니다.');
            return;
        }

        console.log('새로운 영상:', videoData);

        // 중복 체크 (Set 사용으로 O(1) 시간복잡도)
        if (watchedVideoIds.has(videoData.id)) {
            console.warn('중복 영상 감지, 다시 요청합니다.');
            nextVideo(); // 재귀 호출로 다시 시도
            return;
        }

        // 새 영상 정보를 기록
        watchedVideoIds.add(videoData.id);

        // 메모리 관리 - Set 크기가 너무 크면 일부 제거
        if (watchedVideoIds.size > 1000) {
            const oldIds = Array.from(watchedVideoIds).slice(0, 500);
            oldIds.forEach(id => watchedVideoIds.delete(id));
        }

        // 히스토리 관리 - 현재 위치 이후의 기록 제거 (앞으로 가기 방지)
        videoHistory = videoHistory.slice(0, currentVideoIndex + 1);

        // 새 영상을 히스토리에 추가
        videoHistory.push({
            id: videoData.id,
            url: `/@${videoData.uploader.mention}/swipe/video/${videoData.videoLoc}`,
            videoData: videoData
        });
        currentVideoIndex = videoHistory.length - 1;

        // 페이지 이동
        const newUrl = `${location.origin}/@${videoData.uploader.mention}/swipe/video/${videoData.videoLoc}`;
        await transitionToVideo(newUrl);

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
        const prevVideoData = videoHistory[currentVideoIndex];

        console.log('이전 영상으로 이동:', prevVideoData);

        // 이전 영상의 전체 URL로 이동
        let targetUrl;
        if (prevVideoData.url.startsWith('http')) {
            targetUrl = prevVideoData.url;
        } else {
            targetUrl = `${location.origin}${prevVideoData.url}`;
        }

        await transitionToVideo(targetUrl);

    } catch (error) {
        console.error('이전 영상 로딩 실패:', error);
        showErrorMessage('이전 영상을 불러올 수 없습니다.');
        currentVideoIndex++; // 실패시 인덱스 복원
    } finally {
        hideLoadingIndicator();
    }
}

// 부드러운 페이지 전환
async function transitionToVideo(url) {
    try {
        console.log('페이지 전환:', url);

        // 페이드 아웃 효과
        const mainVideo = document.getElementById('main-video');
        if (mainVideo) {
            mainVideo.style.transition = 'opacity 0.3s ease-in-out';
            mainVideo.style.opacity = '0';
        }

        // 잠시 대기 후 페이지 이동
        await new Promise(resolve => setTimeout(resolve, 300));

        // 직접 페이지 이동 (History API 대신)
        window.location.href = url;

    } catch (error) {
        console.error('페이지 전환 실패:', error);
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
    if (loader) {
        loader.remove();
    }
}

// 에러 메시지 표시
function showErrorMessage(message) {
    // 기존 에러 메시지가 있으면 제거
    const existingError = document.querySelector('.error-message');
    if (existingError) {
        existingError.remove();
    }

    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message fixed top-20 left-1/2 transform -translate-x-1/2 bg-red-500 text-white px-6 py-3 rounded-lg shadow-lg z-50';
    errorDiv.textContent = message;

    document.body.appendChild(errorDiv);

    setTimeout(() => {
        if (errorDiv.parentNode) {
            errorDiv.remove();
        }
    }, 3000);
}

// 키보드 단축키 지원
document.addEventListener('keydown', function(e) {
    switch(e.key) {
        case 'ArrowDown':
        case ' ': // 스페이스바
            e.preventDefault();
            nextVideo();
            break;
        case 'ArrowUp':
            e.preventDefault();
            prevVideo();
            break;
    }
});

// 브라우저 뒤로가기/앞으로가기 지원
window.addEventListener('popstate', function(e) {
    console.log('브라우저 네비게이션 감지');
    // 필요하다면 여기서 현재 상태를 업데이트
});

// 디버깅용 함수들
window.debugVideoHistory = function() {
    console.log('현재 히스토리:', videoHistory);
    console.log('현재 인덱스:', currentVideoIndex);
    console.log('시청한 영상 개수:', watchedVideoIds.size);
};

// 페이지 언로드 시 정리
window.addEventListener('beforeunload', function() {
    hideLoadingIndicator();
});
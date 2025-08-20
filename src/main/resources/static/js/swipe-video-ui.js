"use strict";

const videoWrapper = document.getElementById("video-container-fuck");
let startY = 0;

// 시청한 영상 ID들을 추적 (중복 방지용)
let watchedVideoIds = [];
let videoHistory = []; // 이전 영상으로 돌아가기용
let currentVideoIndex = 0;

// 페이지 로드시 현재 영상 ID를 추가
document.addEventListener('DOMContentLoaded', function() {
    const currentVideo = document.getElementById('main-video');
    if (currentVideo) {
        const currentVideoId = parseInt(currentVideo.dataset.videoId);
        watchedVideoIds.push(currentVideoId);
        videoHistory.push({
            id: currentVideoId,
            url: window.location.pathname
        });
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

        const response = await fetch('/api/videos/random', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                excludeIds: watchedVideoIds
            })
        });

        if (!response.ok) {
            throw new Error('영상을 불러올 수 없습니다.');
        }

        const videoData = await response.json();
        console.log(videoData);

        // 새 영상 정보를 기록
        watchedVideoIds.push(videoData.id);

        // 기록이 너무 많으면 오래된 것 제거 (메모리 관리)
        if (watchedVideoIds.length > 1000) {
            watchedVideoIds = watchedVideoIds.slice(-500);
        }

        // 히스토리에 추가
        videoHistory = videoHistory.slice(0, currentVideoIndex + 1); // 앞으로 가기 기록 제거
        videoHistory.push({
            id: videoData.id,
            url: `${location.origin}/${videoData.videoSrc}`
        });
        currentVideoIndex = videoHistory.length - 1;

        // 페이지 이동 (부드러운 전환 효과와 함께)
        await transitionToVideo(`${location.origin}/${videoData.}video/${videoData.id}`);

    } catch (error) {
        console.error('다음 영상 로딩 실패:', error);
        showErrorMessage('다음 영상을 불러올 수 없습니다.');
    } finally {
        hideLoadingIndicator();
    }
}

// 이전 영상으로 돌아가기
async function prevVideo() {
    if (currentVideoIndex > 0) {
        try {
            showLoadingIndicator();

            currentVideoIndex--;
            const prevVideoData = videoHistory[currentVideoIndex];

            await transitionToVideo(prevVideoData.url);

        } catch (error) {
            console.error('이전 영상 로딩 실패:', error);
            showErrorMessage('이전 영상을 불러올 수 없습니다.');
        } finally {
            hideLoadingIndicator();
        }
    } else {
        showErrorMessage('첫 번째 영상입니다.');
    }
}

// 부드러운 페이지 전환
async function transitionToVideo(url) {
    // 페이드 아웃 효과
    const mainVideo = document.getElementById('main-video');
    if (mainVideo) {
        mainVideo.style.transition = 'opacity 0.3s ease-in-out';
        mainVideo.style.opacity = '0';
    }

    // 잠시 대기 후 페이지 이동
    await new Promise(resolve => setTimeout(resolve, 300));

    // History API 사용해서 부드러운 이동
    window.history.pushState(null, '', url);
    window.location.reload();
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
    const errorDiv = document.createElement('div');
    errorDiv.className = 'fixed top-20 left-1/2 transform -translate-x-1/2 bg-red-500 text-white px-6 py-3 rounded-lg shadow-lg z-50';
    errorDiv.textContent = message;

    document.body.appendChild(errorDiv);

    setTimeout(() => {
        errorDiv.remove();
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
    // 브라우저 히스토리 변경시 적절히 처리
    console.log('브라우저 네비게이션 감지');
});
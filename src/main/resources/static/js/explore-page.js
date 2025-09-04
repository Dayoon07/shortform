"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const tags = document.querySelectorAll('.tag');
    const videoContainer = document.getElementById("video-con"); // 더 명확한 ID 사용

    if (!videoContainer) {
        console.error('비디오 컨테이너를 찾을 수 없습니다.');
        return;
    }

    // 태그 클릭 이벤트 처리
    tags.forEach(tag => {
        tag.addEventListener('click', async function() {
            // 이전 활성 태그 제거
            tags.forEach(t => t.classList.remove('active'));

            // 현재 태그를 활성화
            this.classList.add('active');

            const selectedTag = this.getAttribute('data-tag');
            console.log('선택된 태그:', selectedTag);

            if (!selectedTag) {
                console.warn('태그 데이터가 없습니다.');
                return;
            }

            // 비디오 로드
            await loadVideosByTag(selectedTag);
        });
    });

    // 비디오 카드 HTML 생성 함수
    function createVideoCard(video) {
        return `
            <div class="video-card">
                <a href="${location.origin}/@${video.mention}/swipe/video/${video.videoLoc}" 
                   class="relative group cursor-pointer block">
                    <div class="relative aspect-[9/16] bg-gray-900 rounded-lg overflow-hidden">
                        <video 
                            src="${location.origin + video.videoSrc}" 
                            data-src="${video.videoSrc}" 
                            playsinline 
                            muted 
                            preload="metadata"
                            class="lazy-video w-full h-full object-cover">
                        </video>
                        
                        <!-- 호버 오버레이 -->
                        <div class="absolute inset-0 bg-black/20 opacity-0 group-hover:opacity-100 transition-opacity duration-200"></div>

                        <!-- 좋아요 수 -->
                        <div class="absolute bottom-2 left-2 flex items-center space-x-1 bg-black/60 rounded-full px-2 py-1 backdrop-blur-sm">
                            <svg class="w-3 h-3 text-white" fill="currentColor" viewBox="0 0 24 24">
                                <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                            </svg>
                            <span class="text-white text-xs font-medium">${video.likeCount}</span>
                        </div>

                        <!-- 재생 버튼 -->
                        <div class="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                            <div class="w-12 h-12 bg-white/20 rounded-full flex items-center justify-center backdrop-blur-sm border border-white/30">
                                <svg class="w-6 h-6 text-white ml-1" fill="currentColor" viewBox="0 0 24 24">
                                    <path d="M8 5v14l11-7z"/>
                                </svg>
                            </div>
                        </div>
                    </div>
                </a>
                
                <!-- 비디오 정보 -->
                <div class="mt-2 px-1">
                    <a href="${location.origin}/@${video.mention}" 
                       class="block flex items-center space-x-2 mb-1">
                        <img src="${video.profileImgSrc}" 
                             class="w-8 h-8 rounded-full object-cover" 
                             alt="${video.uploaderUsername} 프로필"
                             onerror="this.src='${location.origin}/images/default-profile.png'">
                        <span class="text-white text-md font-semibold truncate">${video.uploaderUsername}</span>
                    </a>

                    <p class="text-gray-300 text-md leading-tight line-clamp-2 mb-1" title="${video.videoTitle}">
                        ${video.videoTitle.length > 25 ? video.videoTitle.substring(0, 25) + '...' : video.videoTitle}
                    </p>

                    <span class="text-gray-400 text-sm">조회수 ${formatViewCount(video.videoViews)}회</span>
                </div>
            </div>
        `;
    }

    // 조회수 포맷팅 함수
    function formatViewCount(views) {
        const num = parseInt(views);
        if (num >= 1000000) {
            return (num / 1000000).toFixed(1) + 'M';
        } else if (num >= 1000) {
            return (num / 1000).toFixed(1) + 'K';
        }
        return num.toString();
    }

    // 로딩 상태 표시 함수
    function showLoading() {
        videoContainer.innerHTML = `
            <div class="w-full flex justify-center items-center py-8">
                <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-white"></div>
                <span class="ml-2 text-white">로딩 중...</span>
            </div>
        `;
    }

    // 빈 상태 표시 함수
    function showEmptyState(hashtag) {
        videoContainer.classList.remove("grid");
        videoContainer.innerHTML = `
            <div class="w-full text-center py-8">
                <div class="text-gray-400 mb-2">
                    <svg class="w-16 h-16 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z"></path>
                    </svg>
                </div>
                <h2 class="text-xl font-semibold text-white mb-2">#${hashtag} 관련 영상이 없습니다</h2>
                <p class="text-gray-400">다른 해시태그를 선택해보세요.</p>
            </div>
        `;
    }

    // 에러 상태 표시 함수
    function showErrorState() {
        videoContainer.innerHTML = `
            <div class="w-full text-center py-8">
                <div class="text-red-400 mb-2">
                    <svg class="w-16 h-16 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                </div>
                <h2 class="text-xl font-semibold text-white mb-2">오류가 발생했습니다</h2>
                <p class="text-gray-400">잠시 후 다시 시도해주세요.</p>
            </div>
        `;
    }

    // 비디오 데이터 로드 함수
    async function loadVideosByTag(selectedTag) {
        try {
            showLoading();

            const response = await fetch(`${location.origin}/api/videos/tag?hashtag=${encodeURIComponent(selectedTag)}`, {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const data = await response.json();
            console.log('받은 데이터:', data);

            if (Array.isArray(data) && data.length > 0) {
                // 그리드 레이아웃 적용 및 비디오 카드들 생성
                videoContainer.classList.add("grid");
                videoContainer.innerHTML = data.map(video => createVideoCard(video)).join('');

                // lazy loading 초기화 (필요한 경우)
                initLazyLoading();
            } else {
                showEmptyState(selectedTag);
            }

        } catch (error) {
            console.error('비디오 로드 중 오류:', error);
            showErrorState();
        }
    }

    // lazy loading 초기화 함수
    function initLazyLoading() {
        const lazyVideos = document.querySelectorAll('.lazy-video');

        if ('IntersectionObserver' in window) {
            const videoObserver = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const video = entry.target;
                        video.src = video.dataset.src;
                        video.classList.remove('lazy-video');
                        videoObserver.unobserve(video);
                    }
                });
            });

            lazyVideos.forEach(video => videoObserver.observe(video));
        }
    }

});
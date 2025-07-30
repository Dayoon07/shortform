document.addEventListener('DOMContentLoaded', function() {
    const video = document.getElementById('main-video');
    const videoOverlay = document.getElementById('video-overlay');
    const playPauseBtn = document.getElementById('play-pause-btn');
    const progressBar = document.getElementById('progress-bar');
    const progressContainer = progressBar.parentElement;

    // 댓글 모달 관련
    const commentBtn = document.getElementById('comment-btn');
    const commentModal = document.getElementById('comment-modal');
    const modalBackdrop = document.getElementById('modal-backdrop');
    const closeModal = document.getElementById('close-modal');
    const popularSort = document.getElementById('popular-sort');
    const recentSort = document.getElementById('recent-sort');

    // 재생/일시정지 버튼
    function updatePlayPauseIcon() {
        playPauseBtn.textContent = video.paused ? '▶️' : '⏸️';
    }

    playPauseBtn.addEventListener('click', () => {
        if (video.paused) {
            video.play();
        } else {
            video.pause();
        }
        updatePlayPauseIcon();
    });

    video.addEventListener('play', updatePlayPauseIcon);
    video.addEventListener('pause', updatePlayPauseIcon);

    // 오버레이 클릭도 재생/정지
    videoOverlay.addEventListener('click', () => {
        if (video.paused) {
            video.play();
        } else {
            video.pause();
        }
        updatePlayPauseIcon();
    });

    // 프로그래스바 업데이트
    video.addEventListener('timeupdate', () => {
        const percent = (video.currentTime / video.duration) * 100;
        progressBar.style.width = percent + '%';
    });

    let isDragging = false;

    progressContainer.addEventListener('mousedown', (e) => {
        isDragging = true;
        updateProgress(e);
    });

    document.addEventListener('mousemove', (e) => {
        if (isDragging) {
            updateProgress(e);
        }
    });

    document.addEventListener('mouseup', () => {
        if (isDragging) {
            isDragging = false;
        }
    });

    function updateProgress(e) {
        const rect = progressContainer.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const ratio = Math.min(Math.max(x / rect.width, 0), 1);
        video.currentTime = ratio * video.duration;
        progressBar.style.width = `${ratio * 100}%`;
    }

    // 초기화
    updatePlayPauseIcon();
    console.log('커스텀 영상 컨트롤 UI 적용 완료');

    // 댓글 모달 열기
    commentBtn.addEventListener('click', () => {
        commentModal.classList.remove('hidden');
        document.body.style.overflow = 'hidden';
        popularComment();
    });

    async function popularComment() {
        const commentContainer = document.getElementById("comment-list-wawawawawa");
        const videoId = commentContainer.dataset.videoId;

        function timeAgo(dateString) {
            const date = new Date(dateString);
            const now = new Date();
            const seconds = Math.floor((now - date) / 1000);

            const units = [
                { name: "년", seconds: 60 * 60 * 24 * 365 },
                { name: "개월", seconds: 60 * 60 * 24 * 30 },
                { name: "일", seconds: 60 * 60 * 24 },
                { name: "시간", seconds: 60 * 60 },
                { name: "분", seconds: 60 },
                { name: "초", seconds: 1 }
            ];

            for (let unit of units) {
                const interval = Math.floor(seconds / unit.seconds);
                if (interval >= 1) {
                    return `${interval}${unit.name} 전`;
                }
            }

            return "방금 전";
        }

        try {
            const res = await fetch(`${location.origin}/api/video/find/comment/popular?id=${videoId}`);

            if (res.ok) {
                const data = await res.json();
                console.log(data);

                if (data != null) {
                    data.forEach(commentData => {
                        commentContainer.innerHTML += `
                            <div class="flex space-x-3">
                                <a href="${location.origin}/@${commentData.mention}" style="cursor: pointer">
                                    <img src="${commentData.profileImgSrc}" alt="프로필" class="w-8 h-8 rounded-full">
                                </a>
                                <div class="flex-1">
                                    <div class="flex items-center space-x-2">
                                        <span class="font-semibold text-sm text-white" style="cursor: pointer" 
                                            onclick="location.href = '${location.origin}/@${commentData.mention}'"
                                        >${commentData.username}</span>
                                        <span class="text-xs text-gray-400">${timeAgo(commentData.createAt)}</span>
                                    </div>
                                    <p class="text-sm text-gray-300 mt-1">${commentData.commentText}</p>
                                    <div class="flex items-center space-x-4 mt-2">
                                        <button class="text-xs text-gray-400 hover:text-white flex items-center space-x-1">
                                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                                            </svg>
                                            <span>${commentData.likeCount == null ? 0 : commentData.likeCount}</span>
                                        </button>
                                        <button class="text-xs text-gray-400 hover:text-white">답글</button>
                                    </div>
                                </div>
                            </div>
                        `;
                    });
                }
            }

        } catch (error) {
            console.error(error);
        }

    }

    // 댓글 모달 닫기
    function closeCommentModal() {
        commentModal.classList.add('hidden');
        document.body.style.overflow = 'auto';
    }

    closeModal.addEventListener('click', closeCommentModal);
    modalBackdrop.addEventListener('click', closeCommentModal);

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && !commentModal.classList.contains('hidden')) {
            closeCommentModal();
        }
    });

    // 댓글 정렬 버튼
    popularSort.addEventListener('click', () => {
        popularSort.className = 'bg-gradient-to-r from-pink-500 to-blue-500 text-white px-4 py-2 rounded-full text-sm font-medium transition-all duration-200';
        recentSort.className = 'bg-gray-700 hover:bg-gray-600 text-white px-4 py-2 rounded-full text-sm font-medium transition-all duration-200';
    });

    recentSort.addEventListener('click', () => {
        recentSort.className = 'bg-gradient-to-r from-pink-500 to-blue-500 text-white px-4 py-2 rounded-full text-sm font-medium transition-all duration-200';
        popularSort.className = 'bg-gray-700 hover:bg-gray-600 text-white px-4 py-2 rounded-full text-sm font-medium transition-all duration-200';
    });

    // 좋아요 버튼 애니메이션
    const likeBtn = document.getElementById('like-btn');
    let isLiked = false;

    likeBtn.addEventListener('click', () => {
        if (!isLiked) {
            likeBtn.querySelector('svg').style.fill = '#ef4444';
            likeBtn.querySelector('svg').style.stroke = '#ef4444';
            likeBtn.classList.add('animate-pulse');
            setTimeout(() => likeBtn.classList.remove('animate-pulse'), 600);
            isLiked = true;
        } else {
            likeBtn.querySelector('svg').style.fill = 'none';
            likeBtn.querySelector('svg').style.stroke = 'currentColor';
            isLiked = false;
        }
    });

    // 초기화 완료
    console.log('Video UI initialized with native controls');

    // 댓글 작성
    document.getElementById("comment-submit-btn").addEventListener("click", async (e) => {
        e.preventDefault();

        const commentText = document.getElementById("commentText");

        if (commentText.value == null || commentText.value === "") {
            alert("댓글을 입력해주세요");
        }

        if (commentText.value != null || commentText.value !== "") {
            const commentFormData = new FormData();
            const videoId = commentText.dataset.videoId;
            const videoCommentContainer = document.getElementById("comment-list-wawawawawa");

            commentFormData.append('commentText', commentText.value);
            commentFormData.append('commentVideoId', videoId);

            try {
                const res = await fetch(`${location.origin}/api/video/insert/comment`, {
                    method: 'POST',
                    body: commentFormData
                });

                if (res.ok) {
                    const data = await res.json();
                    console.log(data);
                    console.log(data.commentText);
                    console.log(data.userObj);

                    videoCommentContainer.innerHTML += `
                        <div class="flex space-x-3">
                            <img src="${data.userObj.profileImgSrc}" alt="프로필" class="w-8 h-8 rounded-full">
                            <div class="flex-1">
                                <div class="flex items-center space-x-2">
                                    <span class="font-semibold text-sm text-white">${data.userObj.username}</span>
                                    <span class="text-xs text-gray-400">방금 전</span>
                                </div>
                                <p class="text-sm text-gray-300 mt-1">${data.commentText}</p>
                                <div class="flex items-center space-x-4 mt-2">
                                    <button class="text-xs text-gray-400 hover:text-white flex items-center space-x-1">
                                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                                        </svg>
                                        <span>0</span>
                                    </button>
                                    <button class="text-xs text-gray-400 hover:text-white">답글</button>
                                </div>
                            </div>
                        </div>
                    `;

                    commentText.value = '';
                }

            } catch (error) {
                console.log(error);
            }
        }
    });
});
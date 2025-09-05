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
        commentContainer.innerHTML = "";
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
                                <a href="${location.origin}/@${commentData.mention}" style="cursor: pointer; width: 36px; height: 36px;">
                                    <img src="${commentData.profileImgSrc}" alt="${commentData.username}님의 프로필" style="width: 36px; height: 36px; padding: 2px; border-radius: 9999px; object-fit: cover; background: linear-gradient(to right, #ec4899, #0ea5e9);" />
                                </a>
                                <div class="flex-1">
                                    <div class="flex items-center space-x-2">
                                        <span class="font-semibold text-md text-white" style="cursor: pointer" 
                                            onclick="location.href = '${location.origin}/@${commentData.mention}'"
                                        >${commentData.username}</span>
                                        <span class="text-sm text-gray-400">${timeAgo(commentData.createAt)}</span>
                                    </div>
                                    <pre style="white-space: pre-wrap; font-family: inherit;">${commentData.commentText}</pre>
                                    <div class="flex items-center space-x-4 mt-2">
                                        <button class="text-md text-gray-400 hover:text-white flex items-center space-x-1 comment-other-user-like-submit-btn" data-comment-id="${commentData.id}">
                                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                                            </svg>
                                            <span>${commentData.likeCount == null ? 0 : commentData.likeCount}</span>
                                        </button>
                                        <button class="text-md text-gray-400 hover:text-white relative comment" data-id="${commentData.id}">답글</button>
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

    async function recentComment() {
        const commentContainer = document.getElementById("comment-list-wawawawawa");
        commentContainer.innerHTML = "";
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
            const res = await fetch(`${location.origin}/api/video/find/comment/recent?id=${videoId}`);

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
                                        <span class="font-semibold text-md text-white" style="cursor: pointer" 
                                            onclick="location.href = '${location.origin}/@${commentData.mention}'"
                                        >${commentData.username}</span>
                                        <span class="text-sm text-gray-400">${timeAgo(commentData.createAt)}</span>
                                    </div>
                                    <pre style="white-space: pre-wrap; font-family: inherit;">${commentData.commentText}</pre>
                                    <div class="flex items-center space-x-4 mt-2">
                                        <button class="text-md text-gray-400 hover:text-white flex items-center space-x-1">
                                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                                            </svg>
                                            <span>${commentData.likeCount == null ? 0 : commentData.likeCount}</span>
                                        </button>
                                        <button class="text-md text-gray-400 hover:text-white">답글</button>
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
        popularComment();
    });

    recentSort.addEventListener('click', () => {
        recentSort.className = 'bg-gradient-to-r from-pink-500 to-blue-500 text-white px-4 py-2 rounded-full text-sm font-medium transition-all duration-200';
        popularSort.className = 'bg-gray-700 hover:bg-gray-600 text-white px-4 py-2 rounded-full text-sm font-medium transition-all duration-200';
        recentComment();
    });

    // 초기화 완료
    console.log('Video UI initialized with native controls');

    // 댓글 작성
    document.getElementById("comment-submit-btn").addEventListener("click", async (e) => {
        e.preventDefault();

        const commentText = document.getElementById("commentText");

        if (commentText.value == null || commentText.value.trim() === "") {
            alert("댓글을 입력해주세요");
            return; // 조건 충족하면 여기서 끝냄
        }

        const commentFormData = new FormData();
        const videoId = commentText.dataset.videoId;
        const videoCommentContainer = document.getElementById("comment-list-wawawawawa");

        commentFormData.append('commentText', commentText.value.trim());
        commentFormData.append('commentVideoId', videoId);

        try {
            const res = await fetch(`${location.origin}/api/video/insert/comment`, {
                method: 'POST',
                body: commentFormData
            });

            if (res.ok) {
                const data = await res.json();
                videoCommentContainer.insertAdjacentHTML('afterbegin', `
                <div class="flex space-x-3">
                    <a href="${location.origin}/@${data.mention}" style="cursor: pointer">
                        <img src="${data.userObj.profileImgSrc}" alt="프로필" class="w-8 h-8 rounded-full">
                    </a>
                    <div class="flex-1">
                        <div class="flex items-center space-x-2">
                            <span class="font-semibold text-md text-white" style="cursor: pointer"
                                  onclick="location.href = '${location.origin}/@${data.mention}'">
                                ${data.userObj.username}
                            </span>
                            <span class="text-sm text-gray-400">방금 전</span>
                        </div>
                        <pre style="white-space: pre-wrap; font-family: inherit;">${data.commentText}</pre>
                        <div class="flex items-center space-x-4 mt-2">
                            <button class="text-md text-gray-400 hover:text-white flex items-center space-x-1">
                                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                          d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                                </svg>
                                <span>0</span>
                            </button>
                            <button class="text-md text-gray-400 hover:text-white">답글</button>
                        </div>
                    </div>
                </div>
            `);

                let currentCount = parseInt(document.getElementById("video-comment-size").textContent.match(/\d+/)[0], 10);
                currentCount += 1;
                document.getElementById("video-comment-size").textContent = `댓글 ${currentCount}개`;

                commentText.value = '';
            }
        } catch (error) {
            console.log(error);
        }
    });

    const likeBtn = document.getElementById('like-btn');
    const likeCount = document.getElementById('like-count');

    if (likeBtn) {
        // 초기 좋아요 상태를 data-is-liked에서 가져오기
        let isLiked = likeBtn.dataset.isLiked === 'true';

        // 페이지 로드 시 초기 상태 설정
        updateLikeUI(isLiked, false); // 애니메이션 없이 초기화

        likeBtn.addEventListener('click', async (e) => {
            const videoId = e.currentTarget.dataset.videoId;
            const heartIcon = likeBtn.querySelector('svg');

            // 클릭 시 즉시 애니메이션 실행
            heartIcon.classList.add('heart-animation');
            setTimeout(() => heartIcon.classList.remove('heart-animation'), 600);

            try {
                const res = await fetch(`${location.origin}/api/video/like`, {
                    method: "POST",
                    body: JSON.stringify({ videoId }),
                    headers: { "Content-Type": "application/json" }
                });

                if (res.ok) {
                    const data = await res.json();
                    console.log(data);
                    isLiked = data.isLiked;

                    // UI 업데이트 (좋아요 개수도 함께)
                    updateLikeUI(isLiked, true);
                    if (likeCount && data.totalLikes !== undefined) {
                        likeCount.textContent = data.totalLikes;
                    }
                } else {
                    const errorData = await res.json();
                    alert(errorData.message || "요청 처리 중 오류가 발생했습니다.");
                }

            } catch (error) {
                console.log(error);
                alert("네트워크 오류가 발생했습니다.");
            }
        });

        // UI 업데이트 함수
        function updateLikeUI(liked, withAnimation = true) {
            const heartIcon = likeBtn.querySelector('svg');

            if (liked) {
                heartIcon.style.fill = '#ef4444';
                heartIcon.style.stroke = '#ef4444';
                heartIcon.style.color = '#ef4444';
            } else {
                heartIcon.style.fill = 'none';
                heartIcon.style.stroke = 'currentColor';
                heartIcon.style.color = 'white';
            }

            // 데이터 속성 업데이트
            likeBtn.dataset.isLiked = liked;
        }
    }

    document.addEventListener("click", async (e) => {
        const btn = e.target.closest(".comment-other-user-like-submit-btn");
        if (!btn) return;

        const commentId = btn.dataset.commentId;
        const countSpan = btn.querySelector("span");
        const heartIcon = btn.querySelector("svg");

        try {
            const res = await fetch(`${location.origin}/api/comment/like/submit?commentId=${commentId}`, {
                method: "POST"
            });

            if (res.ok) {
                const data = await res.json();
                let count = parseInt(countSpan.textContent) || 0;

                if (data.status === "liked") {
                    count += 1;
                    // 하트 색상 빨강
                    heartIcon.classList.remove("text-gray-400");
                    heartIcon.classList.add("text-red-500");
                } else {
                    count -= 1;
                    // 하트 색상 원래대로
                    heartIcon.classList.remove("text-red-500");
                    heartIcon.classList.add("text-gray-400");
                }

                countSpan.textContent = count;
            }
        } catch (error) {
            console.log(error);
        }
    });

    document.getElementById("comment-list-wawawawawa").addEventListener("click", (e) => {
        const btn = e.target.closest("button.comment");
        if (!btn) return;

        console.log("답글 버튼 클릭, 댓글 ID:", btn.dataset.id);

        const existingInput = document.querySelector(".reply-input");
        if (existingInput) existingInput.remove();

        const inputDiv = document.createElement("div");
        inputDiv.className = "reply-input inline-block ml-2";
        inputDiv.innerHTML = `
            <input type="text" data-comment-reply-id="${btn.dataset.id}" placeholder="답글을 입력하세요..." 
                class="px-2 py-1 border rounded text-sm text-black" />
            <button type="button" class="reply-submit px-1 text-md ml-1 border rounded bg-gray-200 text-black">
                작성
            </button>
            <button type="button" class="reply-cancel px-1 text-md ml-1 border rounded bg-gray-300 text-black">
                취소
            </button>
        `;

        const replyButton = inputDiv.querySelector(".reply-submit");
        const cancelButton = inputDiv.querySelector(".reply-cancel");
        const replyInput = inputDiv.querySelector("input");

        replyButton.addEventListener("click", async () => {
            if (replyInput.value.trim() === "") {
                alert("댓글을 입력해주세요");
                return;
            }
            console.log("작성된 답글:", replyInput.value, "댓글 ID:", replyInput.dataset.commentReplyId);

            try {
                const res = await fetch(`${location.origin}/api/insert/comment/reply`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        commentReplyUserId: JSON.parse(localStorage.getItem("user")).id,
                        commentReplyId: replyInput.dataset.commentReplyId,
                        commentReplyText: replyInput.value,
                    })
                });

                if (res.ok) {
                    const data = await res.json();
                    console.log(data);
                }
            } catch (error) {
                console.log(error);
            }

            inputDiv.remove();
        });

        cancelButton.addEventListener("click", () => {
            inputDiv.remove();
        });

        replyInput.addEventListener("keydown", (e) => {
            if (e.key === "Enter") {
                e.preventDefault();
                replyButton.click();
            }
            if (e.key === "Escape") {
                e.preventDefault();
                cancelButton.click();
            }
        });

        // 입력창을 버튼 다음에 삽입
        btn.insertAdjacentElement("afterend", inputDiv);
        replyInput.focus();
    });

});
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
                            <div class="parent-comment-real-${commentData.id}">
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
                                        <div>
                                            <div class="flex items-center space-x-4 mt-2">
                                                <button class="text-md text-gray-400 hover:text-white flex items-center space-x-1 comment-other-user-like-submit-btn" data-comment-id="${commentData.id}">
                                                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                                                    </svg>
                                                    <span>${commentData.likeCount == null ? 0 : commentData.likeCount}</span>
                                                </button>
                                                <button class="text-md text-gray-400 hover:text-white relative comment" data-id="${commentData.id}">작성</button>
                                                <button class="text-md text-gray-400 hover:text-white relative comment-reply" data-id="${commentData.id}">보기</button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        `;
                    });

                    commentContainer.querySelectorAll(".comment-reply").forEach(btn => {
                        btn.addEventListener("click", async (e) => {
                            const v = e.currentTarget.dataset.id;
                            const parent = document.querySelector(`.parent-comment-real-${v}`);

                            // 이미 대댓글이 있으면 아무것도 안함
                            if (parent.querySelector(".reply-container")) {
                                return;
                            }

                            try {
                                const res = await fetch(`${location.origin}/api/find/comment/reply/content?commentId=${v}`, {
                                    method: "POST"
                                });

                                if (res.ok) {
                                    const data = await res.json();
                                    console.log("대댓글 데이터:", data);

                                    // 새 reply-container 생성
                                    const replyContainer = document.createElement("div");
                                    replyContainer.classList.add("reply-container", "ml-12", "mt-3", "space-y-3", "border-l-2", "border-gray-600", "pl-4");

                                    data.forEach(reply => {
                                        // 대댓글 데이터 구조에 맞게 수정
                                        const replyUser = reply.user;
                                        const replyText = reply.commentReplyText;
                                        const replyCreateAt = reply.createAt;

                                        const replyDiv = document.createElement("div");
                                        replyDiv.className = "flex space-x-3 py-2";
                                        replyDiv.innerHTML = `
                                        <a href="${location.origin}/@${replyUser.mention}" style="cursor: pointer; width: 28px; height: 28px;">
                                            <img src="${replyUser.profileImgSrc}" alt="${replyUser.username}님의 프로필" 
                                                 style="width: 28px; height: 28px; padding: 2px; border-radius: 9999px; object-fit: cover; background: linear-gradient(to right, #ec4899, #0ea5e9);" />
                                        </a>
                                        <div class="flex-1">
                                            <div class="flex items-center space-x-2">
                                                <span class="font-semibold text-sm text-white" style="cursor: pointer"
                                                    onclick="location.href='${location.origin}/@${replyUser.mention}'"
                                                >${replyUser.username}</span>
                                                <span class="text-xs text-gray-400">${timeAgo(replyCreateAt)}</span>
                                            </div>
                                            <pre style="white-space: pre-wrap; font-family: inherit; font-size: 0.875rem; color: #e5e7eb; margin: 0;">${replyText}</pre>
                                        </div>
                                    `;
                                        replyContainer.appendChild(replyDiv);
                                    });

                                    parent.appendChild(replyContainer);
                                }

                            } catch (error) {
                                console.log(error);
                            }
                        });
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
                                        <button class="text-md text-gray-400 hover:text-white flex items-center space-x-1 comment-other-user-like-submit-btn" data-comment-id="${commentData.id}">
                                            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                                            </svg>
                                            <span>${commentData.likeCount == null ? 0 : commentData.likeCount}</span>
                                        </button>
                                        <button class="text-md text-gray-400 hover:text-white comment" data-id="${commentData.id}">답글</button>
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

    // 기존 답글 버튼 클릭 이벤트 핸들러 부분 수정함
    document.getElementById("comment-list-wawawawawa").addEventListener("click", (e) => {
        const btn = e.target.closest("button.comment");
        if (!btn) return;

        console.log("답글 버튼 클릭, 댓글 ID:", btn.dataset.id);

        const existingInput = document.querySelector(".reply-input");
        if (existingInput) existingInput.remove();

        const inputDiv = document.createElement("div");
        inputDiv.className = "reply-input mt-3 ml-10 animate-fadeIn";
        inputDiv.innerHTML = `
        <div class="bg-gray-800/50 backdrop-blur-sm rounded-xl border border-gray-700/50 p-4 space-y-3">
            <div class="flex items-center space-x-2 text-sm text-gray-400">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                          d="M3 10h10a8 8 0 018 8v2M3 10l6 6m-6-6l6-6"></path>
                </svg>
                <span>답글 작성</span>
            </div>
            
            <div class="relative">
                <textarea 
                    data-comment-reply-id="${btn.dataset.id}"
                    placeholder="정중하게 답글을 작성해주세요..." 
                    rows="3"
                    class="w-full px-4 py-3 bg-gray-900/70 border border-gray-600/50 rounded-lg 
                           text-white placeholder-gray-400 text-sm
                           focus:ring-2 focus:ring-blue-500/50 focus:border-blue-500/50 
                           focus:outline-none transition-all duration-200
                           resize-none scrollbar-thin scrollbar-thumb-gray-600 scrollbar-track-transparent"
                    maxlength="500"></textarea>
                
                <div class="absolute bottom-2 right-3 text-xs text-gray-500">
                    <span class="char-count">0</span>/500
                </div>
            </div>
            
            <div class="flex items-center justify-between">
                <div class="flex items-center space-x-2 text-xs text-gray-500">
                    <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                        <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd"></path>
                    </svg>
                    <span>Enter로 줄바꿈, Ctrl+Enter로 전송</span>
                </div>
                
                <div class="flex items-center space-x-2">
                    <button type="button" 
                            class="reply-cancel px-4 py-2 text-sm font-medium text-gray-400 
                                   hover:text-gray-200 hover:bg-gray-700/50 
                                   rounded-lg transition-all duration-200
                                   border border-gray-600/30 hover:border-gray-500/50">
                        <svg class="w-4 h-4 inline-block mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                        </svg>
                        취소
                    </button>
                    
                    <button type="button" 
                            class="reply-submit px-4 py-2 text-sm font-medium text-white
                                   bg-gradient-to-r from-blue-500 to-purple-600 
                                   hover:from-blue-600 hover:to-purple-700
                                   disabled:from-gray-600 disabled:to-gray-700 disabled:cursor-not-allowed
                                   rounded-lg transition-all duration-200 transform hover:scale-105 active:scale-95
                                   shadow-lg hover:shadow-xl disabled:shadow-none
                                   relative overflow-hidden group">
                        <div class="absolute inset-0 bg-gradient-to-r from-blue-400 to-purple-500 opacity-0 group-hover:opacity-20 transition-opacity duration-200"></div>
                        <svg class="w-4 h-4 inline-block mr-1 relative z-10" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" 
                                  d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"></path>
                        </svg>
                        <span class="relative z-10">답글 달기</span>
                    </button>
                </div>
            </div>
        </div>
    `;

        const replyButton = inputDiv.querySelector(".reply-submit");
        const cancelButton = inputDiv.querySelector(".reply-cancel");
        const replyTextarea = inputDiv.querySelector("textarea");
        const charCount = inputDiv.querySelector(".char-count");

        replyTextarea.addEventListener("input", () => {
            const length = replyTextarea.value.length;
            charCount.textContent = length;

            // 글자 수에 따른 색상 변경
            if (length > 450) {
                charCount.className = "char-count text-red-400";
            } else if (length > 350) {
                charCount.className = "char-count text-yellow-400";
            } else {
                charCount.className = "char-count text-gray-500";
            }

            // 버튼 활성화/비활성화
            if (length === 0 || length > 500) {
                replyButton.disabled = true;
                replyButton.classList.add("disabled:from-gray-600", "disabled:to-gray-700", "disabled:cursor-not-allowed");
            } else {
                replyButton.disabled = false;
                replyButton.classList.remove("disabled:from-gray-600", "disabled:to-gray-700", "disabled:cursor-not-allowed");
            }
        });

        replyButton.addEventListener("click", async () => {
            if (replyTextarea.value.trim() === "") {
                alert("댓글을 입력해주세요");
                return;
            }

            // 로딩 상태 표시
            replyButton.disabled = true;
            replyButton.innerHTML = `
            <svg class="animate-spin w-4 h-4 inline-block mr-1" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            전송 중...
        `;

            console.log("작성된 답글:", replyTextarea.value, "댓글 ID:", replyTextarea.dataset.commentReplyId);

            try {
                const res = await fetch(`${location.origin}/api/insert/comment/reply`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        commentReplyUserId: JSON.parse(localStorage.getItem("user")).id,
                        commentReplyId: replyTextarea.dataset.commentReplyId,
                        commentReplyText: replyTextarea.value.trim(),
                    })
                });

                if (res.ok) {
                    const data = await res.json();
                    console.log(data);

                    // 성공 피드백
                    replyButton.innerHTML = `
                    <svg class="w-4 h-4 inline-block mr-1 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                    </svg>
                    전송 완료
                `;

                    setTimeout(() => {
                        inputDiv.remove();
                    }, 1000);

                } else {
                    throw new Error('서버 오류');
                }
            } catch (error) {
                console.log(error);

                replyButton.innerHTML = `
                <svg class="w-4 h-4 inline-block mr-1 text-red-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
                전송 실패
            `;
                replyButton.disabled = false;

                setTimeout(() => {
                    replyButton.innerHTML = `
                    <svg class="w-4 h-4 inline-block mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"></path>
                    </svg>
                    답글 달기
                `;
                }, 2000);
            }
        });

        // 기존 기능 유지 (취소 버튼)
        cancelButton.addEventListener("click", () => {
            inputDiv.classList.add("animate-fadeOut");
            setTimeout(() => inputDiv.remove(), 200);
        });

        // 키보드 이벤트 (기존 기능 개선)
        replyTextarea.addEventListener("keydown", (e) => {
            if (e.key === "Enter" && (e.ctrlKey || e.metaKey)) {
                e.preventDefault();
                if (!replyButton.disabled) {
                    replyButton.click();
                }
            }
            if (e.key === "Escape") {
                e.preventDefault();
                cancelButton.click();
            }
        });

        // 자동 높이 조절
        replyTextarea.addEventListener("input", () => {
            replyTextarea.style.height = "auto";
            replyTextarea.style.height = Math.min(replyTextarea.scrollHeight, 120) + "px";
        });

        // 입력창을 버튼 다음에 삽입하고 애니메이션 효과
        btn.closest(".flex").insertAdjacentElement("afterend", inputDiv);
        replyTextarea.focus();

        // 부드러운 스크롤
        setTimeout(() => {
            inputDiv.scrollIntoView({ behavior: "smooth", block: "nearest" });
        }, 100);
    });

});
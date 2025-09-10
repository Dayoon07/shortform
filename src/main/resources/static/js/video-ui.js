document.addEventListener('DOMContentLoaded', function() {
    // DOM 요소 캐싱
    const elements = {
        video: document.getElementById('main-video'),
        videoOverlay: document.getElementById('video-overlay'),
        playPauseBtn: document.getElementById('play-pause-btn'),
        progressBar: document.getElementById('progress-bar'),
        progressContainer: null,
        commentBtn: document.getElementById('comment-btn'),
        commentModal: document.getElementById('comment-modal'),
        modalBackdrop: document.getElementById('modal-backdrop'),
        closeModal: document.getElementById('close-modal'),
        popularSort: document.getElementById('popular-sort'),
        recentSort: document.getElementById('recent-sort'),
        commentContainer: document.getElementById("comment-list-wawawawawa"),
        commentText: document.getElementById("commentText"),
        commentSubmitBtn: document.getElementById("comment-submit-btn"),
        likeBtn: document.getElementById('like-btn'),
        likeCount: document.getElementById('like-count'),
        videoCommentSize: document.getElementById("video-comment-size")
    };

    elements.progressContainer = elements.progressBar?.parentElement;

    // 유틸리티 함수들
    const utils = {
        timeAgo(dateString) {
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
        },

        async fetchJSON(url, options = {}) {
            try {
                const res = await fetch(url, options);
                return res.ok ? await res.json() : null;
            } catch (error) {
                console.error('Fetch error:', error);
                return null;
            }
        },

        createCommentHTML(commentData, isReply = false) {
            const sizeClass = isReply ? 'w-7 h-7' : 'w-8 h-8';
            const textSizeClass = isReply ? 'text-sm' : 'text-md';
            const timeClass = isReply ? 'text-xs' : 'text-sm';

            return `
                <div class="${isReply ? '' : 'parent-comment-real-' + commentData.id}">
                    <div class="flex space-x-3">
                        <a href="${location.origin}/@${commentData.mention || commentData.user?.mention}" 
                           style="cursor: pointer; ${isReply ? 'width: 28px; height: 28px;' : ''}">
                            <img src="${commentData.profileImgSrc || commentData.user?.profileImgSrc}" 
                                 alt="${commentData.username || commentData.user?.username}님의 프로필" 
                                 class="${sizeClass} rounded-full" 
                                 style="${isReply ? 'width: 28px; height: 28px; padding: 2px; border-radius: 9999px; object-fit: cover; background: linear-gradient(to right, #ec4899, #0ea5e9);' : ''}" />
                        </a>
                        <div class="flex-1">
                            <div class="flex items-center space-x-2">
                                <span class="font-semibold ${textSizeClass} text-white" style="cursor: pointer" 
                                      onclick="location.href = '${location.origin}/@${commentData.mention || commentData.user?.mention}'">
                                    ${commentData.username || commentData.user?.username}
                                </span>
                                <span class="${timeClass} text-gray-400">${this.timeAgo(commentData.createAt)}</span>
                            </div>
                            <pre style="white-space: pre-wrap; font-family: inherit; ${isReply ? 'font-size: 0.875rem; color: #e5e7eb; margin: 0;' : ''}">${commentData.commentText || commentData.commentReplyText}</pre>
                            ${!isReply ? this.createCommentActionsHTML(commentData) : ''}
                        </div>
                    </div>
                </div>
            `;
        },

        createCommentActionsHTML(commentData) {
            return `
                <div class="flex items-center space-x-4 mt-2">
                    <button class="text-md text-gray-400 hover:text-white flex items-center space-x-1 comment-other-user-like-submit-btn" data-comment-id="${commentData.id}">
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"></path>
                        </svg>
                        <span>${commentData.likeCount || 0}</span>
                    </button>
                    <button class="text-md text-gray-400 hover:text-white comment" data-id="${commentData.id}">답글</button>
                    <button class="text-md text-gray-400 hover:text-white relative comment-reply" data-id="${commentData.id}">보기</button>
                </div>
            `;
        }
    };

    // 비디오 컨트롤 관련
    const videoController = {
        init() {
            if (!elements.video) return;

            this.updatePlayPauseIcon();
            this.bindEvents();
        },

        updatePlayPauseIcon() {
            if (elements.playPauseBtn) {
                elements.playPauseBtn.textContent = elements.video.paused ? '▶️' : '⏸️';
            }
        },

        togglePlay() {
            if (elements.video.paused) {
                elements.video.play();
            } else {
                elements.video.pause();
            }
            this.updatePlayPauseIcon();
        },

        updateProgress(e) {
            if (!elements.progressContainer || !elements.video.duration) return;

            const rect = elements.progressContainer.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const ratio = Math.min(Math.max(x / rect.width, 0), 1);
            elements.video.currentTime = ratio * elements.video.duration;
            elements.progressBar.style.width = `${ratio * 100}%`;
        },

        bindEvents() {
            // 재생/일시정지 버튼
            elements.playPauseBtn?.addEventListener('click', () => this.togglePlay());

            // 비디오 이벤트
            elements.video.addEventListener('play', () => this.updatePlayPauseIcon());
            elements.video.addEventListener('pause', () => this.updatePlayPauseIcon());

            // 오버레이 클릭
            elements.videoOverlay?.addEventListener('click', () => this.togglePlay());

            // 프로그래스바 업데이트
            elements.video.addEventListener('timeupdate', () => {
                if (elements.video.duration) {
                    const percent = (elements.video.currentTime / elements.video.duration) * 100;
                    elements.progressBar.style.width = percent + '%';
                }
            });

            // 프로그래스바 드래그
            let isDragging = false;

            elements.progressContainer?.addEventListener('mousedown', (e) => {
                isDragging = true;
                this.updateProgress(e);
            });

            document.addEventListener('mousemove', (e) => {
                if (isDragging) this.updateProgress(e);
            });

            document.addEventListener('mouseup', () => {
                isDragging = false;
            });
        }
    };

    // 댓글 관련
    const commentManager = {
        currentSort: 'popular',

        init() {
            this.bindEvents();
        },

        bindEvents() {
            // 모달 열기/닫기
            elements.commentBtn?.addEventListener('click', () => this.openModal());
            elements.closeModal?.addEventListener('click', () => this.closeModal());
            elements.modalBackdrop?.addEventListener('click', () => this.closeModal());

            // ESC 키로 모달 닫기
            document.addEventListener('keydown', (e) => {
                if (e.key === 'Escape' && !elements.commentModal?.classList.contains('hidden')) {
                    this.closeModal();
                }
            });

            // 정렬 버튼
            elements.popularSort?.addEventListener('click', () => this.switchSort('popular'));
            elements.recentSort?.addEventListener('click', () => this.switchSort('recent'));

            // 댓글 작성
            elements.commentSubmitBtn?.addEventListener('click', (e) => this.submitComment(e));

            // 댓글 관련 이벤트 위임
            elements.commentContainer?.addEventListener('click', (e) => this.handleCommentActions(e));

            // 전체 문서에서 댓글 좋아요 처리
            document.addEventListener('click', (e) => this.handleCommentLike(e));
        },

        openModal() {
            elements.commentModal?.classList.remove('hidden');
            if (document.body) document.body.style.overflow = 'hidden';
            this.loadComments();
        },

        closeModal() {
            elements.commentModal?.classList.add('hidden');
            if (document.body) document.body.style.overflow = 'auto';
        },

        switchSort(sortType) {
            this.currentSort = sortType;
            this.updateSortButtons(sortType);
            this.loadComments();
        },

        updateSortButtons(activeSort) {
            const activeClass = 'bg-gradient-to-r from-pink-500 to-blue-500 text-white px-4 py-2 rounded-full text-sm font-medium transition-all duration-200';
            const inactiveClass = 'bg-gray-700 hover:bg-gray-600 text-white px-4 py-2 rounded-full text-sm font-medium transition-all duration-200';

            if (elements.popularSort && elements.recentSort) {
                elements.popularSort.className = activeSort === 'popular' ? activeClass : inactiveClass;
                elements.recentSort.className = activeSort === 'recent' ? activeClass : inactiveClass;
            }
        },

        async loadComments() {
            if (!elements.commentContainer) return;

            const videoId = elements.commentContainer.dataset.videoId;
            if (!videoId) return;

            elements.commentContainer.innerHTML = "";

            const endpoint = this.currentSort === 'popular'
                ? `/api/video/find/comment/popular?id=${videoId}`
                : `/api/video/find/comment/recent?id=${videoId}`;

            const data = await utils.fetchJSON(`${location.origin}${endpoint}`);

            if (data) {
                this.renderComments(data);
            }
        },

        renderComments(comments) {
            comments.forEach(commentData => {
                elements.commentContainer.innerHTML += utils.createCommentHTML(commentData);
            });
        },

        async loadReplies(commentId) {
            const data = await utils.fetchJSON(`${location.origin}/api/find/comment/reply/content?commentId=${commentId}`, {
                method: "POST"
            });

            if (data) {
                const parent = document.querySelector(`.parent-comment-real-${commentId}`);
                if (parent && !parent.querySelector(".reply-container")) {
                    const replyContainer = document.createElement("div");
                    replyContainer.classList.add("reply-container", "ml-12", "mt-3", "border-l-2", "border-gray-600", "pl-4");

                    data.forEach(reply => {
                        const replyDiv = document.createElement("div");
                        replyDiv.className = "flex space-x-3 py-2";
                        replyDiv.innerHTML = utils.createCommentHTML(reply, true);
                        replyContainer.appendChild(replyDiv);
                    });

                    parent.appendChild(replyContainer);
                }
            }
        },

        async submitComment(e) {
            e.preventDefault();

            if (!elements.commentText?.value?.trim()) {
                alert("댓글을 입력해주세요");
                return;
            }

            const formData = new FormData();
            const videoId = elements.commentText.dataset.videoId;

            formData.append('commentText', elements.commentText.value.trim());
            formData.append('commentVideoId', videoId);

            const data = await utils.fetchJSON(`${location.origin}/api/video/insert/comment`, {
                method: 'POST',
                body: formData
            });

            if (data) {
                elements.commentContainer.insertAdjacentHTML('afterbegin', `
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

                // 댓글 수 업데이트
                if (elements.videoCommentSize) {
                    const currentCount = parseInt(elements.videoCommentSize.textContent.match(/\d+/)[0], 10);
                    elements.videoCommentSize.textContent = `댓글 ${currentCount + 1}개`;
                }

                elements.commentText.value = '';
            }
        },

        handleCommentActions(e) {
            const replyViewBtn = e.target.closest('.comment-reply');
            const replyBtn = e.target.closest('button.comment');

            if (replyViewBtn) {
                this.loadReplies(replyViewBtn.dataset.id);
            } else if (replyBtn) {
                this.createReplyInput(replyBtn);
            }
        },

        async handleCommentLike(e) {
            const btn = e.target.closest(".comment-other-user-like-submit-btn");
            if (!btn) return;

            const commentId = btn.dataset.commentId;
            const countSpan = btn.querySelector("span");
            const heartIcon = btn.querySelector("svg");

            const data = await utils.fetchJSON(`${location.origin}/api/comment/like/submit?commentId=${commentId}`, {
                method: "POST"
            });

            if (data) {
                let count = parseInt(countSpan.textContent) || 0;

                if (data.status === "liked") {
                    count += 1;
                    heartIcon.classList.remove("text-gray-400");
                    heartIcon.classList.add("text-red-500");
                } else {
                    count -= 1;
                    heartIcon.classList.remove("text-red-500");
                    heartIcon.classList.add("text-gray-400");
                }

                countSpan.textContent = count;
            }
        },

        createReplyInput(btn) {
            // 기존 입력창 제거
            document.querySelector(".reply-input")?.remove();

            const inputDiv = document.createElement("div");
            inputDiv.className = "reply-input mt-3 ml-10 animate-fadeIn";
            inputDiv.innerHTML = this.getReplyInputHTML(btn.dataset.id);

            btn.closest(".flex").insertAdjacentElement("afterend", inputDiv);
            this.bindReplyInputEvents(inputDiv);

            const textarea = inputDiv.querySelector("textarea");
            textarea.focus();

            setTimeout(() => {
                inputDiv.scrollIntoView({ behavior: "smooth", block: "nearest" });
            }, 100);
        },

        getReplyInputHTML(commentId) {
            return `
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
                            data-comment-reply-id="${commentId}"
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
                            <button type="button" class="reply-cancel px-4 py-2 text-sm font-medium text-gray-400 
                                           hover:text-gray-200 hover:bg-gray-700/50 
                                           rounded-lg transition-all duration-200
                                           border border-gray-600/30 hover:border-gray-500/50">
                                <svg class="w-4 h-4 inline-block mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                                </svg>
                                취소
                            </button>
                            
                            <button type="button" class="reply-submit px-4 py-2 text-sm font-medium text-white
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
        },

        bindReplyInputEvents(inputDiv) {
            const textarea = inputDiv.querySelector("textarea");
            const charCount = inputDiv.querySelector(".char-count");
            const replyButton = inputDiv.querySelector(".reply-submit");
            const cancelButton = inputDiv.querySelector(".reply-cancel");

            // 글자 수 카운트 및 버튼 활성화
            textarea.addEventListener("input", () => {
                const length = textarea.value.length;
                charCount.textContent = length;

                // 글자 수에 따른 색상 변경
                charCount.className = length > 450 ? "char-count text-red-400"
                    : length > 350 ? "char-count text-yellow-400"
                        : "char-count text-gray-500";

                // 버튼 활성화/비활성화
                replyButton.disabled = length === 0 || length > 500;
            });

            // 자동 높이 조절
            textarea.addEventListener("input", () => {
                textarea.style.height = "auto";
                textarea.style.height = Math.min(textarea.scrollHeight, 120) + "px";
            });

            // 키보드 이벤트
            textarea.addEventListener("keydown", (e) => {
                if (e.key === "Enter" && (e.ctrlKey || e.metaKey)) {
                    e.preventDefault();
                    if (!replyButton.disabled) replyButton.click();
                }
                if (e.key === "Escape") {
                    e.preventDefault();
                    cancelButton.click();
                }
            });

            // 취소 버튼
            cancelButton.addEventListener("click", () => {
                inputDiv.classList.add("animate-fadeOut");
                setTimeout(() => inputDiv.remove(), 200);
            });

            // 전송 버튼
            replyButton.addEventListener("click", () => this.submitReply(textarea, replyButton, inputDiv));
        },

        async submitReply(textarea, replyButton, inputDiv) {
            if (textarea.value.trim() === "") {
                alert("댓글을 입력해주세요");
                return;
            }

            // 로딩 상태
            replyButton.disabled = true;
            replyButton.innerHTML = `
                <svg class="animate-spin w-4 h-4 inline-block mr-1" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                전송 중...
            `;

            const user = JSON.parse(localStorage.getItem("user") || "{}");
            const data = await utils.fetchJSON(`${location.origin}/api/insert/comment/reply`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    commentReplyUserId: user.id,
                    commentReplyId: textarea.dataset.commentReplyId,
                    commentReplyText: textarea.value.trim(),
                })
            });

            if (data) {
                replyButton.innerHTML = `
                    <svg class="w-4 h-4 inline-block mr-1 text-green-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
                    </svg>
                    전송 완료
                `;
                setTimeout(() => inputDiv.remove(), 1000);
            } else {
                this.handleReplyError(replyButton);
            }
        },

        handleReplyError(replyButton) {
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
    };

    // 좋아요 관련
    const likeManager = {
        init() {
            if (!elements.likeBtn) return;

            this.isLiked = elements.likeBtn.dataset.isLiked === 'true';
            this.updateLikeUI(this.isLiked, false);
            this.bindEvents();
        },

        bindEvents() {
            elements.likeBtn.addEventListener('click', (e) => this.toggleLike(e));
        },

        async toggleLike(e) {
            const videoId = e.currentTarget.dataset.videoId;
            const heartIcon = elements.likeBtn.querySelector('svg');

            // 애니메이션 실행
            heartIcon.classList.add('heart-animation');
            setTimeout(() => heartIcon.classList.remove('heart-animation'), 600);

            const data = await utils.fetchJSON(`${location.origin}/api/video/like`, {
                method: "POST",
                body: JSON.stringify({ videoId }),
                headers: { "Content-Type": "application/json" }
            });

            if (data) {
                this.isLiked = data.isLiked;
                this.updateLikeUI(this.isLiked, true);

                if (elements.likeCount && data.totalLikes !== undefined) {
                    elements.likeCount.textContent = data.totalLikes;
                }
            } else {
                alert("요청 처리 중 오류가 발생했습니다.");
            }
        },

        updateLikeUI(liked, withAnimation = true) {
            const heartIcon = elements.likeBtn.querySelector('svg');

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
            elements.likeBtn.dataset.isLiked = liked;
        }
    };

    // 초기화
    function init() {
        videoController.init();
        commentManager.init();
        likeManager.init();

        console.log('커스텀 영상 컨트롤 UI 적용 완료');
        console.log('Video UI initialized with native controls');
    }

    // 모든 기능 초기화
    init();

});
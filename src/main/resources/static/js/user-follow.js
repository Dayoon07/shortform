// 개선된 팔로우 JavaScript (중복 클릭 완전 방지)
document.addEventListener('DOMContentLoaded', function() {
    const followBtn = document.getElementById('user-follow-btn') || document.getElementById('user-following-btn');
    let isProcessing = false; // 처리 중 플래그
    let lastClickTime = 0; // 마지막 클릭 시간
    const CLICK_DELAY = 1000; // 1초 지연
    const profileEditBtn = document.getElementById("user-profile-edit-btn");

    if (profileEditBtn) {
        profileEditBtn.addEventListener("click", (e) => {
            e.preventDefault();

            // 현재 사용자 정보 가져오기 (실제 구현에서는 API 호출이나 다른 방법으로 가져올 수 있음)
            const currentUser = {
                username: document.getElementById("h1-but-username")?.textContent || '',
                mail: document.getElementById("user-mail-nothing-aaaaaaaaa")?.textContent || '',
                mention: document.getElementById("small-tag-but-just-user-mention")?.textContent || '',
                bio: document.getElementById("biobiobio")?.textContent || '',
                profileImgSrc: document.getElementById("profile-img-src").src || ''
            };

            const modal = document.createElement("div");
            modal.style.position = "fixed";
            modal.style.left = '0';
            modal.style.top = '0';
            modal.style.width = '100%';
            modal.style.height = '100%';
            modal.style.backgroundColor = 'rgba(0, 0, 0, 0.5)';
            modal.style.zIndex = '1000';

            modal.innerHTML = `
                <div class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-white p-6 rounded-lg shadow-lg w-96 max-w-full">
                    <h2 class="text-xl font-bold mb-4">프로필 수정</h2>
                    
                    <form id="profile-edit-form" class="space-y-4 text-black">
                        <!-- 프로필 이미지 -->
                        <div>
                            <label class="block text-sm font-medium mb-2">프로필 이미지</label>
                            <div class="flex items-center space-x-4">
                                <img id="preview-img" src="${currentUser.profileImgSrc}" alt="프로필 미리보기" 
                                     class="w-16 h-16 rounded-full object-cover border">
                                <input type="file" id="profile-img-input" accept="image/*" 
                                       class="text-sm text-gray-500 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100">
                            </div>
                        </div>
        
                        <!-- 사용자명 -->
                        <div>
                            <label for="username" class="block text-sm font-medium mb-2">사용자명</label>
                            <input type="text" id="username" name="username" value="${currentUser.username}"
                                   class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                   required>
                        </div>
        
                        <!-- 이메일 -->
                        <div>
                            <label for="mail" class="block text-sm font-medium mb-2">이메일</label>
                            <input type="email" id="mail" name="mail" value="${currentUser.mail}"
                                   class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                   required>
                        </div>
        
                        <!-- 사용자 멘션 -->
                        <div>
                            <label for="mention" class="block text-sm font-medium mb-2">사용자 멘션</label>
                            <div class="flex">
                                <span class="inline-flex items-center px-3 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm">@</span>
                                <input type="text" id="mention" name="mention" value="${currentUser.mention.replace('@', '')}"
                                       class="flex-1 px-3 py-2 border border-gray-300 rounded-r-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                       required>
                            </div>
                        </div>
        
                        <!-- 자기소개 -->
                        <div>
                            <label for="bio" class="block text-sm font-medium mb-2">자기소개</label>
                            <textarea id="bio" name="bio" rows="4" 
                                      class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                      placeholder="자신을 소개해주세요...">${currentUser.bio}</textarea>
                        </div>
        
                        <!-- 버튼들 -->
                        <div class="flex space-x-3 pt-4">
                            <button type="submit" class="flex-1 px-4 py-2 bg-blue-500 text-white rounded-md bg-gradient-to-r from-pink-500 to-sky-500 hover:from-pink-600 hover:to-sky-600">
                                저장
                            </button>
                            <button type="button" id="close-modal" class="flex-1 px-4 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-gray-500">
                                취소
                            </button>
                        </div>
                    </form>
                </div>
            `;

            document.body.appendChild(modal);

            // 프로필 이미지 미리보기 기능
            const profileImgInput = document.getElementById('profile-img-input');
            const previewImg = document.getElementById('preview-img');

            profileImgInput.addEventListener('change', (e) => {
                const file = e.target.files[0];
                if (file) {
                    const reader = new FileReader();
                    reader.onload = (e) => {
                        previewImg.src = e.target.result;
                    };
                    reader.readAsDataURL(file);
                }
            });

            // 폼 제출 처리
            const form = document.getElementById('profile-edit-form');
            form.addEventListener('submit', async (e) => {
                e.preventDefault();

                const formData = new FormData(form);
                formData.append("profileImgSrc", previewImg.src);
                const updatedUser = {
                    username: formData.get('username'),
                    mail: formData.get('mail'),
                    mention: '@' + formData.get('mention'),
                    bio: formData.get('bio'),
                    profileImgSrc: previewImg.src
                };

                try {
                    const res = await fetch(`${location.origin}/api/user/update`, {
                        method: "POST",
                        body: formData,
                    });

                    if (res.ok) {
                        const data = await res.json();
                        console.log(data);
                    }

                } catch (error) {
                    console.log(error);
                }

                console.log('업데이트된 사용자 정보:', updatedUser);

                // 실제 DOM 업데이트 (선택사항)
                updateProfileDisplay(updatedUser);

                // 성공 메시지
                alert('프로필이 성공적으로 업데이트되었습니다!');

                // 모달 닫기
                modal.remove();
            });

            // 모달 닫기 기능
            document.getElementById("close-modal").addEventListener("click", () => {
                modal.remove();
            });

            // 모달 배경 클릭 시 닫기
            modal.addEventListener('click', (e) => {
                if (e.target === modal) {
                    modal.remove();
                }
            });

            // ESC 키로 모달 닫기
            document.addEventListener('keydown', function escapeHandler(e) {
                if (e.key === 'Escape') {
                    modal.remove();
                    document.removeEventListener('keydown', escapeHandler);
                }
            });
        });
    }

    // 프로필 화면 업데이트 함수 (선택사항)
    function updateProfileDisplay(userData) {
        const usernameEl = document.querySelector('.username');
        const emailEl = document.querySelector('.user-email');
        const mentionEl = document.querySelector('.user-mention');
        const bioEl = document.querySelector('.user-bio');
        const profileImgEl = document.querySelector('.profile-img');

        if (usernameEl) usernameEl.textContent = userData.username;
        if (emailEl) emailEl.textContent = userData.mail;
        if (mentionEl) mentionEl.textContent = userData.mention;
        if (bioEl) bioEl.textContent = userData.bio;
        if (profileImgEl) profileImgEl.src = userData.profileImgSrc;
    }

    if (followBtn) {
        followBtn.addEventListener('click', function(e) {
            e.preventDefault();

            const currentTime = Date.now();

            // 이미 처리 중이거나 1초 이내 재클릭인 경우 무시
            if (isProcessing || (currentTime - lastClickTime) < CLICK_DELAY) {
                console.log('요청이 이미 처리 중이거나 너무 빠른 클릭입니다.');
                return;
            }

            lastClickTime = currentTime;
            isProcessing = true;

            const mention = this.getAttribute('data-mention');
            const originalText = this.querySelector('span').textContent;
            const originalClasses = this.className;

            // 버튼 상태를 "처리 중"으로 변경
            this.disabled = true;
            this.style.opacity = '0.6';
            this.style.cursor = 'not-allowed';
            this.querySelector('span').textContent = '처리 중...';

            // 요청 취소를 위한 AbortController
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 10000); // 10초 타임아웃

            fetch(`${location.origin}/api/follow/toggle`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `mention=${encodeURIComponent(mention)}`,
                signal: controller.signal
            })
                .then(response => {
                    clearTimeout(timeoutId);

                    if (!response.ok) {
                        throw new Error(`HTTP error! status: ${response.status}`);
                    }
                    return response.json();
                })
                .then(data => {
                    if (data.success) {
                        // UI 업데이트
                        updateFollowButton(data.isFollowing);
                        updateFollowCounts(data.followerCount, data.followingCount);
                        showNotification(data.message, 'success');
                    } else {
                        // 실패한 경우 원래 상태로 복구
                        restoreButtonState(originalText, originalClasses);
                        showNotification(data.message, 'error');
                    }
                })
                .catch(error => {
                    clearTimeout(timeoutId);
                    console.error('Error:', error);

                    // 원래 상태로 복구
                    restoreButtonState(originalText, originalClasses);

                    if (error.name === 'AbortError') {
                        showNotification('요청 시간이 초과되었습니다.', 'error');
                    } else {
                        showNotification('네트워크 오류가 발생했습니다.', 'error');
                    }
                })
                .finally(() => {
                    // 처리 완료
                    isProcessing = false;

                    // 버튼 상태 복구
                    const currentBtn = document.getElementById('user-follow-btn') || document.getElementById('user-following-btn');
                    if (currentBtn) {
                        currentBtn.disabled = false;
                        currentBtn.style.opacity = '1';
                        currentBtn.style.cursor = 'pointer';
                    }
                });
        });
    }

    // 버튼 상태 복구 함수
    function restoreButtonState(originalText, originalClasses) {
        const btn = document.getElementById('user-follow-btn') || document.getElementById('user-following-btn');
        if (btn) {
            btn.className = originalClasses;
            btn.querySelector('span').textContent = originalText;
        }
    }

    // 팔로우 버튼 UI 업데이트 (개선됨)
    function updateFollowButton(isFollowing) {
        const btn = document.getElementById('user-follow-btn') || document.getElementById('user-following-btn');
        if (!btn) return;

        const span = btn.querySelector('span');
        const svg = btn.querySelector('svg');

        // 애니메이션 효과 추가
        btn.style.transform = 'scale(0.95)';
        setTimeout(() => {
            btn.style.transform = 'scale(1)';
        }, 150);

        if (isFollowing) {
            // 팔로잉 상태
            btn.id = 'user-following-btn';
            btn.className = 'bg-gray-600 hover:bg-red-600 px-8 py-2 rounded-md transition-all duration-200 flex items-center space-x-2';
            span.textContent = '팔로잉';
            if (svg) {
                svg.setAttribute('fill', 'none');
                svg.setAttribute('stroke', 'currentColor');
                svg.innerHTML = '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>';
            }

            // 호버 시 "언팔로우" 텍스트 표시
            btn.addEventListener('mouseenter', function() {
                span.textContent = '언팔로우';
            });
            btn.addEventListener('mouseleave', function() {
                span.textContent = '팔로잉';
            });

        } else {
            // 팔로우 안함 상태
            btn.id = 'user-follow-btn';
            btn.className = 'bg-gradient-to-r from-pink-500 to-sky-500 hover:from-pink-600 hover:to-sky-600 px-8 py-2 rounded-md transition-all duration-200 flex items-center space-x-2';
            span.textContent = '팔로우';
            if (svg) {
                svg.setAttribute('fill', 'none');
                svg.setAttribute('stroke', 'currentColor');
                svg.innerHTML = '<path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>';
            }

            // 호버 이벤트 제거
            btn.removeEventListener('mouseenter', function() {});
            btn.removeEventListener('mouseleave', function() {});
        }
    }

    // 팔로워/팔로잉 수 업데이트 (애니메이션 효과 추가)
    function updateFollowCounts(followerCount, followingCount) {
        const followerCountElement = document.getElementById('follower-count');
        const followingCountElement = document.getElementById('following-count');

        // 카운트 애니메이션 효과
        function animateCount(element, newValue) {
            if (!element) return;

            element.style.transform = 'scale(1.2)';
            element.style.color = '#3b82f6'; // 파란색으로 강조

            setTimeout(() => {
                element.textContent = newValue;
                element.style.transform = 'scale(1)';
                element.style.color = ''; // 원래 색상으로 복구
            }, 200);
        }

        if (followerCount !== undefined) {
            animateCount(followerCountElement, followerCount);
        }
        if (followingCount !== undefined) {
            animateCount(followingCountElement, followingCount);
        }
    }

    // 개선된 알림 메시지 표시
    function showNotification(message, type) {
        // 기존 알림이 있으면 제거
        const existingNotification = document.querySelector('.notification-toast');
        if (existingNotification) {
            existingNotification.remove();
        }

        const notification = document.createElement('div');
        notification.className = `notification-toast absolute top-4 right-64 px-6 py-3 rounded-lg text-white z-50 transform translate-x-full transition-transform duration-300 ${
            type === 'success' ? 'bg-green-500' : 'bg-red-500'
        }`;

        // 아이콘 추가
        const icon = type === 'success' ? '✓' : '✕';
        notification.innerHTML = `
            <div class="flex items-center space-x-2">
                <span class="text-lg">${icon}</span>
                <span>${message}</span>
            </div>
        `;

        document.body.appendChild(notification);

        // 슬라이드 인 애니메이션
        setTimeout(() => {
            notification.style.transform = 'translate-x-0';
        }, 100);

        // 3초 후 슬라이드 아웃 후 제거
        setTimeout(() => {
            notification.style.transform = 'translate-x-full';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        }, 3000);
    }

    // 기존 비디오 호버 기능 (변경 없음)
    const videoCards = document.querySelectorAll('.relative.group.cursor-pointer');

    videoCards.forEach(card => {
        const video = card.querySelector('video');

        if (video) {
            card.addEventListener("mouseover", function() {
                video.currentTime = 0;
                video.play().catch(e => {
                    console.log('Video play failed:', e);
                });
            });

            card.addEventListener('mouseout', function() {
                video.pause();
                video.currentTime = 0;
            });

            video.addEventListener('click', function(e) {
                e.preventDefault();
                if (video.paused) {
                    video.play();
                } else {
                    video.pause();
                }
            });
        }
    });

    const findFollowingUserBtn = document.getElementById("find-following-user-btn");
    const findFollowUserBtn = document.getElementById("find-follow-user-btn");

    const followModal = document.getElementById("follow-modal");
    const followModalTitle = document.getElementById("follow-modal-title");
    const followModalList = document.getElementById("follow-modal-list");
    const followModalClose = document.getElementById("follow-modal-close");

    function openFollowModal(title, data) {
        followModalTitle.textContent = title;
        followModalList.innerHTML = ""; // 기존 목록 초기화

        data.forEach(user => {
            const li = document.createElement("li");
            li.className = "flex items-center gap-4 p-4 cursor-pointer hover:bg-gray-200 transition duration-300";
            li.onclick = () => {
                location.href = `${location.origin}/@${user.mention}`;
            };

            const img = document.createElement("img");
            img.src = user.profileImgSrc || "/default-profile.png";
            img.alt = `${user.username}의 프로필 이미지`;
            img.className = "w-12 h-12 rounded-full object-cover";

            const infoDiv = document.createElement("div");
            infoDiv.className = "flex flex-col";

            const username = document.createElement("span");
            username.className = "font-medium text-gray-800";
            username.textContent = user.username;

            const mention = document.createElement("span");
            mention.className = "text-sm text-gray-500";
            mention.textContent = user.mention;

            infoDiv.appendChild(username);
            infoDiv.appendChild(mention);
            li.appendChild(img);
            li.appendChild(infoDiv);
            followModalList.appendChild(li);
        });

        followModal.classList.remove("hidden");
    }

    followModalClose.addEventListener("click", () => {
        followModal.classList.add("hidden");
    });

    document.addEventListener("keydown", function(e) {
        if (e.key === "Escape") followModal.classList.add("hidden");
    });

    // 팔로잉 목록 버튼 이벤트
    if (findFollowingUserBtn) {
        findFollowingUserBtn.addEventListener("click", async () => {
            const id = findFollowingUserBtn.dataset.followingId;
            try {
                const res = await fetch(`${location.origin}/api/follow/user/following/list?id=${id}`);
                if (res.ok) {
                    const data = await res.json();
                    openFollowModal("팔로우 목록", data);
                }
            } catch (err) {
                console.error(err);
            }
        });
    }

    // 팔로워 목록 버튼 이벤트
    if (findFollowUserBtn) {
        findFollowUserBtn.addEventListener("click", async () => {
            const id = findFollowUserBtn.dataset.followId;
            try {
                const res = await fetch(`${location.origin}/api/follow/user/follower/list?id=${id}`);
                if (res.ok) {
                    const data = await res.json();
                    openFollowModal("팔로워 목록", data);
                }
            } catch (err) {
                console.error(err);
            }
        });
    }

});
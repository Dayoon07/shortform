// 개선된 팔로우 JavaScript (중복 클릭 완전 방지)
document.addEventListener('DOMContentLoaded', function() {
    const followBtn = document.getElementById('user-follow-btn');
    let isProcessing = false; // 처리 중 플래그
    let lastClickTime = 0; // 마지막 클릭 시간
    const CLICK_DELAY = 1000; // 1초 지연

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

            fetch('/api/follow/toggle', {
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
                    followBtn.disabled = false;
                    followBtn.style.opacity = '1';
                    followBtn.style.cursor = 'pointer';
                });
        });
    }

    // 버튼 상태 복구 함수
    function restoreButtonState(originalText, originalClasses) {
        const btn = document.getElementById('user-follow-btn');
        if (btn) {
            btn.className = originalClasses;
            btn.querySelector('span').textContent = originalText;
        }
    }

    // 팔로우 버튼 UI 업데이트 (개선됨)
    function updateFollowButton(isFollowing) {
        const btn = document.getElementById('user-follow-btn');
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
            btn.className = 'bg-gradient-to-t from-pink-500 to-sky-500 hover:from-pink-600 hover:to-sky-600 px-8 py-2 rounded-md transition-all duration-200 flex items-center space-x-2';
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
});
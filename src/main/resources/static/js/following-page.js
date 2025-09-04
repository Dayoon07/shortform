"use strict";

document.addEventListener("DOMContentLoaded", () => {

    const followingCancel = document.querySelectorAll("#user-following-btn");
    followingCancel.forEach(btn => {
        btn.addEventListener("click", async (e) => {
            // 이벤트 객체와 현재 타겟 요소 안전성 체크
            if (!e || !e.currentTarget) {
                console.error("이벤트 또는 타겟 요소가 없습니다.");
                return;
            }

            const button = e.currentTarget;
            const mention = button.dataset.mention;

            if (!mention) {
                console.error("멘션 데이터가 없습니다.");
                return;
            }

            button.disabled = true;
            button.textContent = "처리 중...";

            try {
                const res = await fetch(`${location.origin}/api/follow/toggle`, {
                    method: "POST",
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: `mention=${encodeURIComponent(mention)}`
                });

                if (res.ok) {
                    const data = await res.json();
                    console.log(data);
                    console.log("팔로우 취소됨!");

                    let parentDiv = null;

                    // 1차 시도: follow-div 클래스 찾기
                    if (button.closest) {
                        parentDiv = button.closest(".follow-div");
                    }

                    // 2차 시도: bg-gray-800 클래스를 포함하는 div 찾기
                    if (!parentDiv && button.closest) {
                        parentDiv = button.closest('div[class*="bg-gray-800"]');
                    }

                    // 3차 시도: 직접 부모 요소들을 탐색
                    if (!parentDiv) {
                        let element = button.parentElement;
                        while (element && element !== document.body) {
                            if (element.classList.contains('follow-div') ||
                                element.className.includes('bg-gray-800')) {
                                parentDiv = element;
                                break;
                            }
                            element = element.parentElement;
                        }
                    }

                    if (parentDiv) {
                        parentDiv.remove();
                        document.getElementById("following-cnt").textContent = parseFloat(document.getElementById("following-cnt").textContent) - 1;
                        setTimeout(() => {
                            const remainingFollows = document.querySelectorAll(".follow-div, div[class*='bg-gray-800']");
                            if (remainingFollows.length === 0) location.reload();

                            const $msgDiv = document.createElement("div");
                            $msgDiv.style.position = "fixed";
                            $msgDiv.style.left = "50%";
                            $msgDiv.style.top = "20px";
                            $msgDiv.style.translate = "-50%, 0px";
                            $msgDiv.style.backgroundColor = "gray";
                            $msgDiv.className = "text-white px-10 py-2 rounded shadow-md z-50";
                            $msgDiv.innerText = data.message;
                            document.body.appendChild($msgDiv);

                            setTimeout(() => $msgDiv.remove(), 2000);
                        }, 100);
                    } else {
                        console.warn("부모 요소를 찾을 수 없어서 페이지를 새로고침합니다.");
                        location.reload();
                    }
                } else {
                    console.error("팔로우 취소 실패:", res.status);
                    button.disabled = false;
                    button.innerHTML = '<span class="whitespace-nowrap">팔로우 취소</span><svg class="w-4 h-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>';
                }
            } catch (error) {
                console.error("에러:", error);
                button.disabled = false;
                button.innerHTML = '<span class="whitespace-nowrap">팔로우 취소</span><svg class="w-4 h-4 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>';
            }
        });
    });

});
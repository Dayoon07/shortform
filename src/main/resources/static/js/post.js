"use strict";

document.addEventListener("DOMContentLoaded", () => {

    document.querySelectorAll(".post-like-btn").forEach(btn => {
        btn.addEventListener("click", async (e) => {
            const uuid = btn.dataset.communityUuid;
            const likeCntEl = btn.querySelector(".like-cnt");
            let currentCount = parseInt(likeCntEl.textContent.replace(/\D/g, ''), 10);

            try {
                const res = await fetch(`${location.origin}/api/post/like?communityUuid=${uuid}`, {
                    method: "POST"
                });

                if (res.ok) {
                    const data = await res.json();
                    console.log(data);

                    currentCount = data.like ? currentCount + 1 : Math.max(0, currentCount - 1);
                    likeCntEl.textContent = `좋아요 ${currentCount}`;
                    btn.querySelector("svg").classList.toggle("text-red-500", data.like);
                }
            } catch (err) {
                console.error(err);
            }
        });
    });

        document.getElementById("comment-submit-btn").addEventListener("click", async () => {
            const text = document.getElementById("commentText").value.trim();
            const uuid = document.getElementById("community-comment-form").dataset.communityUuid;

            if (!text) {
                alert("댓글 내용을 입력해주세요.");
                return;
            }

            try {
                const res = await fetch(`${location.origin}/api/community/${uuid}/comment/write`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        content: text
                    })
                });

            if (res.ok) {
                const data = await res.json();
                console.log(data);
            } else {
                alert("댓글 작성에 실패했습니다.");
            }

            } catch (e) {
                console.error(e);
                alert("오류가 발생했습니다.");
            }
        });













});

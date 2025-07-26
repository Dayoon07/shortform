"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const followBtn = document.getElementById("user-follow-btn");

    if (followBtn) {
        followBtn.addEventListener("click", async () => {
            const mention = followBtn.dataset.mention;

            try {
                const res = await fetch(`${location.origin}/api/follow`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({ mention: mention })
                });

                if (res.ok) {
                    const data = await res.json();
                    console.log("팔로우 성공:", data);
                    console.log(this);
                } else {
                    const errText = await res.text();
                    console.error("팔로우 실패:", errText);
                }
            } catch (error) {
                console.error("요청 에러:", error);
            }
        });
    }
});

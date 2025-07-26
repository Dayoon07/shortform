"use strict";

let isUsernameAvailable = true;
let isUsernameAvailable2 = true;

document.addEventListener("DOMContentLoaded", () => {

    document.getElementById("loginBtn").onclick = () => {
        document.getElementById("loginModal").classList.remove("hidden");
    };
    document.getElementById("signupBtn").onclick = () => {
        document.getElementById("signupModal").classList.remove("hidden");
    };
    document.getElementById("closeLoginModal").onclick = () => {
        document.getElementById("loginModal").classList.add("hidden");
    };
    document.getElementById("closeSignupModal").onclick = () => {
        document.getElementById("signupModal").classList.add("hidden");
    };
    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            document.getElementById("loginModal").classList.add("hidden");
            document.getElementById("signupModal").classList.add("hidden");
        }
    });

    const profileInput = document.getElementById("profileImgPath");
    const preview = document.getElementById("profilePreview");
    const uploadText = document.getElementById("uploadText");
    const dropArea = document.getElementById("dropArea");

    const step1 = document.getElementById("signupStep1");
    const step2 = document.getElementById("signupStep2");

    // 프로필 이미지 미리보기
    function previewImage(file) {
        if (file.size > 1024 * 1024 * 3) {
            alert("프로필 이미지 최대 용량은 3MB입니다.");
            profileInput.value = "";
            return;
        }

        const reader = new FileReader();
        reader.onload = (e) => {
            preview.src = e.target.result;
            preview.classList.remove("hidden");
            uploadText.style.display = "none";
        };
        reader.readAsDataURL(file);
    }

    // 파일 업로드 이벤트
    profileInput.addEventListener("change", (e) => {
        const file = e.target.files[0];
        if (file) previewImage(file);
    });

    // 드래그앤드롭 처리
    dropArea.addEventListener("dragover", (e) => {
        e.preventDefault();
        dropArea.classList.add("border-gray-400", "bg-gray-400/10");
    });

    dropArea.addEventListener("dragleave", () => {
        dropArea.classList.remove("border-gray-400", "bg-gray-400/10");
    });

    dropArea.addEventListener("drop", (e) => {
        e.preventDefault();
        dropArea.classList.remove("border-gray-400", "bg-gray-400/10");

        const file = e.dataTransfer.files[0];
        if (file) {
            profileInput.files = e.dataTransfer.files;
            previewImage(file);
        }
    });

    // 다음 버튼 → 사용자 정보 입력 단계로 전환
    document.getElementById("nextToStep2Btn").addEventListener("click", () => {
        if (!profileInput.files.length) {
            alert("프로필 이미지를 업로드해주세요.");
            return;
        }
        step1.classList.add("hidden");
        step2.classList.remove("hidden");
    });

    // 가입 버튼 → REST API 호출
    document.getElementById("submitSignupBtn").addEventListener("click", async () => {
        const email = document.getElementById("emailInput").value.trim();
        const username = document.getElementById("usernameInput").value.trim();
        const password = document.getElementById("passwordInput").value;
        const confirm = document.getElementById("confirmPasswordInput").value;
        const profile = profileInput.files[0];

        if (!email || !username || !password || !confirm) {
            alert("모든 정보를 입력해주세요.");
            return;
        }
        if (password !== confirm) {
            alert("비밀번호가 일치하지 않습니다.");
            return;
        }

        // FormData로 전송 (멀티파트 업로드)
        const formData = new FormData();
        formData.append("email", email);
        formData.append("username", username);
        formData.append("password", password);
        formData.append("profileImage", profile);

        try {
            const res = await fetch(`${location.origin}/api/user/signup`, {
                method: "POST",
                body: formData
            });

            if (res.ok) {
                showSignupSuccess();
                document.getElementById("signupModal").classList.add("hidden");
            } else {
                const msg = await res.text();
                alert("회원가입 실패: " + msg);
            }
        } catch (err) {
            alert("에러 발생: " + err.message);
        }
    });

    // 회원가입 완료 알림 표시
    function showSignupSuccess() {
        const alertDiv = document.createElement("div");
        alertDiv.innerText = "회원가입이 완료되었습니다.";
        alertDiv.className = "fixed top-4 left-1/2 transform -translate-x-1/2 bg-green-600 text-white px-6 py-3 rounded-xl shadow-lg z-[9999]";
        document.body.appendChild(alertDiv);

        setTimeout(() => {
            alertDiv.remove();
        }, 3000);
    }

    document.getElementById("usernameInput").addEventListener("input", function () {
        let username = document.getElementById("usernameInput").value;

        // 사용자 이름 중복 여부를 체크
        fetch(`${location.origin}/api/user/chk/username?username=${username}`)
            .then(response => response.json())
            .then(data => {
                isUsernameAvailable = data; // 중복 여부 상태 업데이트

                if (isUsernameAvailable) {
                    document.getElementById('username-feedback').innerText = "사용 가능한 이름입니다.";
                    document.getElementById('username-feedback').style.color = "green";
                } else {
                    document.getElementById('username-feedback').innerText = "이미 사용 중인 이름입니다.";
                    document.getElementById('username-feedback').style.color = "red";
                }
                if (document.getElementById("usernameInput").value == "") {
                    document.getElementById('username-feedback').innerText = "이름을 입력해주세요.";
                    document.getElementById('username-feedback').style.color = "red";
                }

            });
    });

    document.getElementById("emailInput").addEventListener("input", function () {
        let emailVal = document.getElementById("emailInput").value;

        // 사용자 이름 중복 여부를 체크
        fetch(`${location.origin}/api/user/chk/mail?mail=${emailVal}`)
            .then(response => response.json())
            .then(data => {
                isUsernameAvailable2 = data; // 중복 여부 상태 업데이트

                if (isUsernameAvailable2) {
                    document.getElementById('mail-feedback').innerText = "사용 가능한 이메일입니다.";
                    document.getElementById('mail-feedback').style.color = "green";
                } else {
                    document.getElementById('mail-feedback').innerText = "이미 사용 중인 이메일입니다.";
                    document.getElementById('mail-feedback').style.color = "red";
                }
                if (document.getElementById("emailInput").value == "") {
                    document.getElementById('mail-feedback').innerText = "이메일을 입력해주세요.";
                    document.getElementById('mail-feedback').style.color = "red";
                }

            });
    });

    document.getElementById("loginSubmitBtn").addEventListener("click", async (e) => {
        e.preventDefault(); // 폼 전송 막기

        const username = document.getElementById("loginUsername").value.trim();
        const password = document.getElementById("loginPassword").value.trim();

        if (!username || !password) {
            alert("아이디와 비밀번호를 모두 입력해주세요.");
            return;
        }

        try {
            const response = await fetch(`${location.origin}/api/user/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ username, password })
            });

            const data = await response.json();

            if (response.ok && data.success) {
                // 예: 로그인 성공 후 페이지 새로고침 또는 이동
                window.location.reload();
            } else {
                alert("로그인 실패: " + data.message);
            }

        } catch (error) {
            console.error("로그인 요청 오류:", error);
            alert("로그인 중 오류가 발생했습니다.");
        }
    });

});

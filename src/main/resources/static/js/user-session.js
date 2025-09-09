"use strict";

// 모달 관리 클래스
class ModalManager {
    constructor() {
        this.init();
    }

    init() {
        this.bindEvents();
        this.setupEscapeKey();
    }

    bindEvents() {
        // 로그인 모달 열기
        document.getElementById("loginBtn")?.addEventListener("click", () => this.openModal("loginModal"));
        document.getElementById("loginBtn2")?.addEventListener("click", () => this.openModal("loginModal"));

        // 회원가입 모달 열기
        document.getElementById("signupBtn")?.addEventListener("click", () => this.openModal("signupModal"));
        document.getElementById("signupBtn2")?.addEventListener("click", () => this.openModal("signupModal"));

        // 모달 닫기
        document.getElementById("closeLoginModal")?.addEventListener("click", () => this.closeModal("loginModal"));
        document.getElementById("closeSignupModal")?.addEventListener("click", () => this.closeModal("signupModal"));
    }

    setupEscapeKey() {
        document.addEventListener("keydown", (e) => {
            if (e.key === "Escape") {
                this.closeModal("loginModal");
                this.closeModal("signupModal");
            }
        });
    }

    openModal(modalId) {
        document.getElementById(modalId)?.classList.remove("hidden");
    }

    closeModal(modalId) {
        document.getElementById(modalId)?.classList.add("hidden");
    }
}

// 프로필 이미지 업로드 관리 클래스
class ProfileImageUploader {
    constructor() {
        this.profileInput = document.getElementById("profileImgPath");
        this.preview = document.getElementById("profilePreview");
        this.uploadText = document.getElementById("uploadText");
        this.dropArea = document.getElementById("dropArea");
        this.maxFileSize = 1024 * 1024 * 3; // 3MB
        this.init();
    }

    init() {
        if (!this.profileInput || !this.preview || !this.uploadText || !this.dropArea) return;

        this.bindEvents();
    }

    bindEvents() {
        this.profileInput.addEventListener("change", (e) => this.handleFileSelect(e));
        this.setupDragAndDrop();
    }

    setupDragAndDrop() {
        this.dropArea.addEventListener("dragover", (e) => this.handleDragOver(e));
        this.dropArea.addEventListener("dragleave", () => this.handleDragLeave());
        this.dropArea.addEventListener("drop", (e) => this.handleDrop(e));
    }

    handleFileSelect(e) {
        const file = e.target.files[0];
        if (file) this.previewImage(file);
    }

    handleDragOver(e) {
        e.preventDefault();
        this.dropArea.classList.add("border-gray-400", "bg-gray-400/10");
    }

    handleDragLeave() {
        this.dropArea.classList.remove("border-gray-400", "bg-gray-400/10");
    }

    handleDrop(e) {
        e.preventDefault();
        this.dropArea.classList.remove("border-gray-400", "bg-gray-400/10");

        const file = e.dataTransfer.files[0];
        if (file) {
            this.profileInput.files = e.dataTransfer.files;
            this.previewImage(file);
        }
    }

    previewImage(file) {
        if (file.size > this.maxFileSize) {
            alert("프로필 이미지 최대 용량은 3MB입니다.");
            this.profileInput.value = "";
            return;
        }

        const reader = new FileReader();
        reader.onload = (e) => {
            this.preview.src = e.target.result;
            this.preview.classList.remove("hidden");
            this.uploadText.style.display = "none";
        };
        reader.readAsDataURL(file);
    }

    hasFile() {
        return this.profileInput?.files.length > 0;
    }

    getFile() {
        return this.profileInput?.files[0];
    }
}

// 입력 검증 관리 클래스
class ValidationManager {
    constructor() {
        this.isUsernameAvailable = true;
        this.isEmailAvailable = true;
        this.init();
    }

    init() {
        this.setupUsernameValidation();
        this.setupEmailValidation();
    }

    setupUsernameValidation() {
        const usernameInput = document.getElementById("usernameInput");
        if (!usernameInput) return;

        usernameInput.addEventListener("input", () => this.validateUsername());
    }

    setupEmailValidation() {
        const emailInput = document.getElementById("emailInput");
        if (!emailInput) return;

        emailInput.addEventListener("input", () => this.validateEmail());
    }

    async validateUsername() {
        const username = document.getElementById("usernameInput").value;
        const feedbackElement = document.getElementById('username-feedback');

        if (!username) {
            this.setFeedback(feedbackElement, "이름을 입력해주세요.", "red");
            return;
        }

        try {
            const response = await fetch(`${location.origin}/api/user/chk/username?username=${username}`);
            const data = await response.json();
            this.isUsernameAvailable = data;

            const message = this.isUsernameAvailable ? "사용 가능한 이름입니다." : "이미 사용 중인 이름입니다.";
            const color = this.isUsernameAvailable ? "green" : "red";
            this.setFeedback(feedbackElement, message, color);
        } catch (error) {
            console.error("Username validation error:", error);
        }
    }

    async validateEmail() {
        const email = document.getElementById("emailInput").value;
        const feedbackElement = document.getElementById('mail-feedback');

        if (!email) {
            this.setFeedback(feedbackElement, "이메일을 입력해주세요.", "red");
            return;
        }

        try {
            const response = await fetch(`${location.origin}/api/user/chk/mail?mail=${email}`);
            const data = await response.json();
            this.isEmailAvailable = data;

            const message = this.isEmailAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.";
            const color = this.isEmailAvailable ? "green" : "red";
            this.setFeedback(feedbackElement, message, color);
        } catch (error) {
            console.error("Email validation error:", error);
        }
    }

    setFeedback(element, message, color) {
        if (element) {
            element.innerText = message;
            element.style.color = color;
        }
    }

    isValid() {
        return this.isUsernameAvailable && this.isEmailAvailable;
    }
}

// 회원가입 관리 클래스
class SignupManager {
    constructor(profileUploader, validator) {
        this.profileUploader = profileUploader;
        this.validator = validator;
        this.step1 = document.getElementById("signupStep1");
        this.step2 = document.getElementById("signupStep2");
        this.init();
    }

    init() {
        this.bindEvents();
    }

    bindEvents() {
        document.getElementById("nextToStep2Btn")?.addEventListener("click", () => this.goToStep2());
        document.getElementById("submitSignupBtn")?.addEventListener("click", () => this.submitSignup());
    }

    goToStep2() {
        if (!this.profileUploader.hasFile()) {
            alert("프로필 이미지를 업로드해주세요.");
            return;
        }
        this.step1?.classList.add("hidden");
        this.step2?.classList.remove("hidden");
    }

    async submitSignup() {
        const formData = this.collectFormData();
        if (!formData) return;

        try {
            const response = await fetch(`${location.origin}/api/user/signup`, {
                method: "POST",
                body: formData
            });

            if (response.ok) {
                document.getElementById("signupModal")?.classList.add("hidden");
                this.showSignupSuccess();
                this.resetSignupForm();
            } else {
                const message = await response.text();
                alert("회원가입 실패: " + message);
            }
        } catch (error) {
            alert("에러 발생: " + error.message);
        }
    }

    resetSignupForm() {
        // Step을 1단계로 되돌리기
        this.step1?.classList.remove("hidden");
        this.step2?.classList.add("hidden");

        // 모든 입력 필드 초기화
        const inputs = [
            "emailInput",
            "usernameInput",
            "passwordInput",
            "confirmPasswordInput"
        ];

        inputs.forEach(inputId => {
            const input = document.getElementById(inputId);
            if (input) input.value = "";
        });

        // 프로필 이미지 초기화
        this.resetProfileImage();

        // 유효성 검사 피드백 초기화
        this.resetValidationFeedback();

        // 유효성 검사 상태 초기화
        if (this.validator) {
            this.validator.isUsernameAvailable = true;
            this.validator.isEmailAvailable = true;
        }
    }

    // 프로필 이미지 초기화 메서드 (새로 추가)
    resetProfileImage() {
        if (this.profileUploader) {
            // 파일 입력 초기화
            if (this.profileUploader.profileInput) {
                this.profileUploader.profileInput.value = "";
            }

            // 미리보기 이미지 숨기기
            if (this.profileUploader.preview) {
                this.profileUploader.preview.classList.add("hidden");
                this.profileUploader.preview.src = "";
            }

            // 업로드 텍스트 다시 표시
            if (this.profileUploader.uploadText) {
                this.profileUploader.uploadText.style.display = "block";
            }
        }
    }

    // 유효성 검사 피드백 초기화 메서드 (새로 추가)
    resetValidationFeedback() {
        const feedbackElements = [
            "username-feedback",
            "mail-feedback"
        ];

        feedbackElements.forEach(elementId => {
            const element = document.getElementById(elementId);
            if (element) {
                element.innerText = "";
                element.style.color = "";
            }
        });
    }

    collectFormData() {
        const email = document.getElementById("emailInput")?.value.trim();
        const username = document.getElementById("usernameInput")?.value.trim();
        const password = document.getElementById("passwordInput")?.value;
        const confirm = document.getElementById("confirmPasswordInput")?.value;
        const profile = this.profileUploader.getFile();

        if (!email || !username || !password || !confirm) {
            alert("모든 정보를 입력해주세요.");
            return null;
        }

        if (password !== confirm) {
            alert("비밀번호가 일치하지 않습니다.");
            return null;
        }

        if (!this.validator.isValid()) {
            alert("입력 정보를 확인해주세요.");
            return null;
        }

        const formData = new FormData();
        formData.append("email", email);
        formData.append("username", username);
        formData.append("password", password);
        formData.append("profileImage", profile);

        return formData;
    }

    showSignupSuccess() {
        const alertDiv = document.createElement("div");
        alertDiv.innerHTML = "회원가입이 <br/> 완료되었습니다.";
        alertDiv.style.position = "absolute";
        alertDiv.style.left = "50%";
        alertDiv.style.top = "50px";
        alertDiv.style.transform = 'translate(-50%, -20px)';
        alertDiv.style.backgroundColor = "rgb(22, 163, 74)";
        alertDiv.className = "text-white px-10 py-2 rounded shadow-md z-50 text-center";
        document.body.appendChild(alertDiv);

        setTimeout(() => alertDiv.remove(), 3000);
    }
}

// 로그인 관리 클래스
class LoginManager {
    constructor() {
        this.init();
    }

    init() {
        this.bindEvents();
    }

    bindEvents() {
        document.getElementById("loginSubmitBtn")?.addEventListener("click", (e) => this.submitLogin(e));
    }

    async submitLogin(e) {
        e.preventDefault();

        const username = document.getElementById("loginUsername")?.value.trim();
        const password = document.getElementById("loginPassword")?.value.trim();

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
                localStorage.setItem("user", JSON.stringify(data.user));
                location.href = location.origin + "?login=success&message=" + encodeURIComponent(data.message);
            } else {
                alert("로그인 실패: " + data.message);
            }
        } catch (error) {
            console.error("로그인 요청 오류:", error);
            alert("로그인 중 오류가 발생했습니다.");
        }
    }
}

// 검색 관리 클래스
class SearchManager {
    constructor() {
        this.init();
    }

    init() {
        this.bindEvents();
        this.setupDocumentEvents();
    }

    bindEvents() {
        document.getElementById("search-form-submit-btn")?.addEventListener("click", (e) => this.handleSearchSubmit(e));
        document.getElementById("search-btn-but-app-bar")?.addEventListener("click", (e) => this.openSearchModal(e));

        // 클래스 기반 검색 버튼들
        document.querySelectorAll(".search-btn-but-app-bar").forEach(btn => {
            btn.addEventListener("click", (e) => this.openSearchModal(e));
        });
    }

    setupDocumentEvents() {
        document.addEventListener("keydown", (e) => this.handleEscapeKey(e));
        document.addEventListener("click", (e) => this.handleOutsideClick(e));
    }

    handleSearchSubmit(e) {
        const inputTag = document.querySelector("input[name='q']");
        if (inputTag?.value) {
            document.getElementById("search-form")?.submit();
        }
    }

    handleEscapeKey(e) {
        if (e.key === "Escape") {
            this.removeSearchPopup();
            this.removeSearchModal();
        }
    }

    handleOutsideClick(e) {
        const popup = document.querySelector(".search-popup");
        if (popup && !popup.contains(e.target)) {
            popup.remove();
        }
    }

    removeSearchPopup() {
        document.querySelector(".search-popup")?.remove();
    }

    removeSearchModal() {
        document.getElementById("search-modal-popup-what")?.remove();
    }

    async showSearchSuggestions(userId) {
        try {
            const response = await fetch(`${location.origin}/api/user/search/list?id=${userId}`);

            if (!response.ok) return;

            const data = await response.json();
            const popup = this.createSearchPopup(data);
            document.body.appendChild(popup);
        } catch (error) {
            console.error("Search suggestions error:", error);
        }
    }

    createSearchPopup(data) {
        const popup = document.createElement("div");
        popup.classList.add("search-popup", "absolute", "top-32", "left-2", "w-60", "h-auto",
            "bg-gray-800", "z-90", "text-lg", "rounded-3xl", "shadow-lg");

        const itemsToShow = Math.min(data.length, 7);
        for (let i = 0; i < itemsToShow; i++) {
            const item = document.createElement("div");
            item.className = "flex justify-between items-center py-1.5 pl-4 pr-2 text-md cursor-pointer rounded-full text-white hover:bg-gray-700 transition duration-200";
            item.id = `item-search-id-${data[i].id}`;
            item.addEventListener("click", () => location.href = `${location.origin}/search?q=${data[i].searchedWord}`);

            const textDiv = document.createElement("div");
            textDiv.className = "flex-1 truncate pr-3";
            textDiv.textContent = data[i].searchedWord;

            const deleteBtn = document.createElement("div");
            deleteBtn.className = "flex-shrink-0 w-9 h-9 pb-1 rounded-full flex items-center justify-center text-gray-400 hover:text-white hover:bg-gray-500 transition duration-200";
            deleteBtn.dataset.searchId = data[i].id;
            deleteBtn.innerHTML = "&times;";
            deleteBtn.addEventListener("click", (e) => this.deleteSearchWord(e));

            item.appendChild(textDiv);
            item.appendChild(deleteBtn);
            popup.appendChild(item);
        }

        return popup;
    }

    async deleteSearchWord(e) {
        e.stopPropagation();

        const sid = e.currentTarget.dataset.searchId;
        // console.log(sid);
        // console.log(document.querySelector(`#item-search-id-${sid} > div`));
        try {
            const res = await fetch(
                `${location.origin}/api/search/list/delete`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        id: JSON.parse(localStorage.getItem("user")).id,
                        searchWord: document.querySelector(`#item-search-id-${sid} > div`).textContent.trim()
                    })
                }
            );

            if (res.ok) {
                const data = await res.text();
                // console.log(data);

                const itemToRemove = document.getElementById(`item-search-id-${sid}`);
                // console.log(itemToRemove);

                if (itemToRemove) {
                    itemToRemove.remove();
                    // console.log("아이템이 성공적으로 삭제되었습니다.");
                } else {
                    console.error("삭제할 아이템을 찾을 수 없습니다.");
                }
            } else {
                console.error("삭제 실패:", res.status);
            }
        } catch (error) {
            console.log(error);
        }
    }

    openSearchModal(e) {
        // 이미 모달이 열려 있으면 새로 열지 않음
        if (document.getElementById("search-modal-popup-what")) return;

        const modal = this.createSearchModal();
        document.body.appendChild(modal);

        // 사용자 ID가 있으면 검색 기록 표시
        if (e.currentTarget.dataset.id) {
            this.addSearchHistory(modal, e.currentTarget.dataset.id);
        }
    }


    async deleteSearchWordWtf(e) {
        e.stopPropagation();

        const sid = e.currentTarget.dataset.searchId;
        const targetElement = document.querySelector(`#item-search-id-${sid}`);
        console.log(targetElement);

        try {
            const res = await fetch(
                `${location.origin}/api/search/list/delete`, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        id: JSON.parse(localStorage.getItem("user")).id,
                        searchWord: document.querySelectorAll(`#item-search-id-${sid} > div`)[0].textContent
                    })
                }
            );

            if (res.ok) {
                const data = await res.text();
                console.log(data);
                const itemToRemove = document.getElementById(`item-search-id-${sid}`);

                if (itemToRemove) {
                    itemToRemove.remove();
                } else {
                    console.error("삭제할 아이템을 찾을 수 없습니다.");
                }
            } else {
                console.error("삭제 실패:", res.status);
            }
        } catch (error) {
            console.log(error);
        }
    }

    createSearchModal() {
        const modal = document.createElement("div");
        modal.classList.add(
            "absolute", "top-0", "left-0", "w-full", "h-full",
            "bg-white/20", "backdrop-blur-md", "border-b", "border-white/30",
            "px-4", "px-3", "py-2", "py-1"
        );
        modal.style.zIndex = 777;
        modal.id = "search-modal-popup-what";

        modal.innerHTML = `
           <div class="flex items-center py-2">
               <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" 
                   viewBox="0 0 20 20"
                   onclick="document.getElementById('search-modal-popup-what').remove()" 
                   fill="none" stroke="currentColor" stroke-width="2" 
                   stroke-linecap="round" stroke-linejoin="round" 
                   style="color: white; margin-right: 10px; cursor: pointer; margin-bottom: 6px; filter: drop-shadow(0 1px 2px rgba(0,0,0,0.3));">
                   <line x1="19" y1="12" x2="5" y2="12" />
                   <polyline points="12 19 5 12 12 5" />
               </svg>
               <form action="/search" id="search-form" method="get" autocomplete="off" style="position: relative; width: 100%;">
                   <button type="submit" class="absolute top-2.5 left-2.5 p-0 bg-transparent border-none cursor-pointer">
                       <svg class="w-6 h-6 text-white/80 hover:text-white" fill="currentColor" viewBox="0 0 24 24" style="filter: drop-shadow(0 1px 2px rgba(0,0,0,0.3));">
                           <path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
                       </svg>
                   </button>
                   <input type="text" name="q" placeholder="검색" id="search-input-text" maxlength="100"
                       class="w-full pl-10 pr-3 py-2 rounded-full bg-white/20 backdrop-blur-sm border border-white/30 text-white placeholder-white/70 focus:outline-none focus:ring-2 focus:ring-white/50 focus:bg-white/25"
                       required style="box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
               </form>
               <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" 
                   stroke-linejoin="round" class="lucide lucide-x-icon lucide-x" onclick="document.getElementById('search-modal-popup-what').remove()" 
                   style="margin-bottom: 5px; cursor: pointer; color: white; filter: drop-shadow(0 1px 2px rgba(0,0,0,0.3));">
                   <path d="M18 6 6 18"/>
                   <path d="m6 6 12 12"/>
               </svg>
           </div>
       `;

        return modal;
    }

    async addSearchHistory(modal, userId) {
        try {
            const response = await fetch(`${location.origin}/api/user/search/list?id=${userId}`);
            const data = await response.json();

            data.slice(0, 30).forEach(item => {
                const historyItem = document.createElement("div");
                historyItem.className = "flex justify-between items-center";
                historyItem.id = `item-search-id-${item.id}`;

                historyItem.innerHTML = `
                    <div class="py-2 pr-4 pl-12 cursor-pointer rounded-full hover:bg-black/70 hover:backdrop-blur-xs transition duration-300"
                        onclick="location.href='${location.origin}/search?q=${item.searchedWord}'">
                        ${item.searchedWord}
                    </div>
                    <div class="delete-btn cursor-pointer px-2 py-1 hover:bg-red-600 rounded" 
                         data-search-id="${item.id}">
                           &times;
                    </div>
                `;

                const deleteBtn = historyItem.querySelector('.delete-btn');
                deleteBtn.addEventListener('click', (e) => this.deleteSearchWord(e));

                modal.appendChild(historyItem);
            });
        } catch (error) {
            console.error("Search history error:", error);
        }
    }
}

// 애플리케이션 초기화 클래스
class App {
    constructor() {
        this.modalManager = new ModalManager();
        this.profileUploader = new ProfileImageUploader();
        this.validator = new ValidationManager();
        this.signupManager = new SignupManager(this.profileUploader, this.validator);
        this.loginManager = new LoginManager();
        this.searchManager = new SearchManager();
    }
}

// 레거시 함수 (기존 코드와의 호환성을 위해 유지)
async function akakakakakakaka(param) {
    const searchManager = new SearchManager();
    await searchManager.showSearchSuggestions(param);
}

function showToast(message, type = 'info') {
    const $div = document.createElement("div");
    $div.style.position = "absolute";
    $div.style.left = "50%";
    $div.style.top = "50px";
    $div.style.transform = 'translate(-50%, -20px)';
    $div.style.backgroundColor = type === "success" ? "rgb(22, 163, 74)" : "rgb(107, 114, 128)";
    $div.className = "text-white px-10 py-2 rounded shadow-md z-50";
    $div.innerText = message;
    document.body.appendChild($div);
    setTimeout(() => $div.remove(), 2000);
}











// DOM 로드 시 애플리케이션 초기화
window.addEventListener('DOMContentLoaded', () => {
    new App();

    const params = new URLSearchParams(location.search);
    if (params.get('login') === 'success') {
        const message = params.get('message') || '로그인 성공!';
        showToast(message, 'success');
        // URL 파라미터 제거
        history.replaceState({}, '', location.pathname);
    }

    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', function(e) {
            if ('vibrate' in navigator) {
                navigator.vibrate(50);
            }

            this.style.transform = 'scale(0.95)';
            this.style.backgroundColor = 'rgba(255, 255, 255, 0.1)';

            setTimeout(() => {
                this.style.transform = '';
                this.style.backgroundColor = '';
            }, 150);
        });
    });

    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('touchstart', function(e) {
            this.style.transform = 'scale(0.95)';
            this.style.backgroundColor = 'rgba(255, 255, 255, 0.1)';
        });

        item.addEventListener('touchend', function(e) {
            setTimeout(() => {
                this.style.transform = '';
                this.style.backgroundColor = '';
            }, 150);
        });

        item.addEventListener('touchcancel', function(e) {
            this.style.transform = '';
            this.style.backgroundColor = '';
        });
    });

});
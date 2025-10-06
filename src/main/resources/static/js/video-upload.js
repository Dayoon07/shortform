const dropzone = document.getElementById("dropzone");
const fileInput = document.getElementById("fileInput");
const selectBtn = document.getElementById("selectFileBtn");
const uploadArea = document.getElementById("upload-area");
const previewArea = document.getElementById("preview-area");
const videoPreview = document.getElementById("video-preview");
const fileInfo = document.getElementById("file-info");

// 파일 로딩 진행률 요소
const fileProgress = document.getElementById("file-progress");
const fileProgressBar = document.getElementById("file-progress-bar");
const fileProgressText = document.getElementById("file-progress-text");

// 서버 업로드 진행률 요소
const uploadProgress = document.getElementById("upload-progress");
const uploadProgressBar = document.getElementById("upload-progress-bar");
const uploadProgressText = document.getElementById("upload-progress-text");

const uploadModal = document.getElementById("upload-modal");
const modalVideoPreview = document.getElementById("modal-video-preview");
const closeModal = document.getElementById("close-modal");
const cancelUpload = document.getElementById("cancel-upload");
const publishVideo = document.getElementById("publish-video");

// 현재 선택된 파일을 저장
let currentFile = null;

// 클릭 시 파일 선택
selectBtn.addEventListener("click", () => {
    fileInput.click();
});

// 파일 선택 시 처리
fileInput.addEventListener("change", handleFiles);

// 드래그 앤 드롭 기능
dropzone.addEventListener("dragover", (e) => {
    e.preventDefault();
    dropzone.classList.add("bg-gray-200");
});

dropzone.addEventListener("dragleave", () => {
    dropzone.classList.remove("bg-gray-200");
});

dropzone.addEventListener("drop", (e) => {
    e.preventDefault();
    dropzone.classList.remove("bg-gray-200");
    const files = e.dataTransfer.files;
    if (files.length > 0) {
        fileInput.files = files;
        handleFiles();
    }
});

function handleFiles() {
    const file = fileInput.files[0];
    if (!file) return;

    // 파일 유효성 검사
    if (!file.type.startsWith('video/')) {
        alert('동영상 파일만 업로드 가능합니다.');
        return;
    }

    const allowedExt = ['mp4', 'mov', 'avi', 'wmv'];
    const ext = file.name.split('.').pop().toLowerCase();
    if (!allowedExt.includes(ext)) {
        alert('허용되지 않은 동영상 형식입니다. (mp4, mov, avi, wmv만 가능)');
        return;
    }

    // 파일 크기 검사 (150MB)
    const maxSize = 1024 * 1024 * 150;
    if (file.size > maxSize) {
        alert('파일 크기가 150MB를 초과합니다.');
        return;
    }

    currentFile = file;
    processFileOnClient(file);
}

// 클라이언트에서 파일 처리 (진행률 시뮬레이션)
function processFileOnClient(file) {
    // 업로드 영역 숨기기
    uploadArea.classList.add("hidden");

    // 파일 처리 진행률 표시
    fileProgress.classList.remove("hidden");

    // FileReader로 파일 읽기 시뮬레이션
    const reader = new FileReader();
    let progress = 0;

    // 진행률 시뮬레이션
    const progressInterval = setInterval(() => {
        progress += Math.random() * 20;
        if (progress >= 100) {
            progress = 100;
            clearInterval(progressInterval);

            // 파일 처리 완료
            setTimeout(() => {
                showPreview(file);
                fileProgress.classList.add("hidden");
                showUploadModal();
            }, 300);
        }

        fileProgressBar.style.width = progress + '%';
        fileProgressText.textContent = Math.round(progress) + '%';
    }, 100);

    // 실제 파일 읽기 (미리보기용)
    reader.onload = function(e) {
        // 파일 읽기 완료되면 미리보기 준비
    };
    reader.readAsDataURL(file);
}

function showPreview(file) {
    // 미리보기 표시
    previewArea.classList.remove("hidden");

    // 동영상 미리보기 설정
    const url = URL.createObjectURL(file);
    videoPreview.src = url;
    modalVideoPreview.src = url;

    // 파일 정보 표시
    const fileSize = (file.size / (1024 * 1024)).toFixed(1);
    fileInfo.textContent = `${file.name} (${fileSize}MB)`;
}

// 서버에 실제 업로드
async function uploadToServer(file, videoData) {
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        const formData = new FormData();

        // 파일과 메타데이터 함께 전송
        formData.append('video', file);
        formData.append('title', videoData.title);
        formData.append('description', videoData.description);
        formData.append('hashtags', videoData.hashtags);
        formData.append('visibility', videoData.visibility);
        formData.append('commentsAllowed', videoData.commentsAllowed);

        // 업로드 진행률 추적
        xhr.upload.addEventListener('progress', (e) => {
            if (e.lengthComputable) {
                const percent = Math.round((e.loaded / e.total) * 100);
                uploadProgressBar.style.width = percent + '%';
                uploadProgressText.textContent = percent + '%';
            }
        });

        // 완료 처리
        xhr.addEventListener('load', () => {
            if (xhr.status >= 200 && xhr.status < 300) {
                try {
                    const response = JSON.parse(xhr.responseText);
                    resolve(response);
                } catch (e) {
                    resolve({ success: true });
                }
            } else {
                reject(new Error(`HTTP ${xhr.status}: ${xhr.statusText}`));
            }
        });

        // 오류 처리
        xhr.addEventListener('error', () => {
            reject(new Error('네트워크 오류가 발생했습니다.'));
        });

        // 요청 전송
        xhr.open('POST', '/api/upload/video');
        xhr.send(formData);
    });
}

function showUploadModal() {
    uploadModal.classList.remove("hidden");
    document.body.style.overflow = "hidden";
}

function hideUploadModal() {
    uploadModal.classList.add("hidden");
    document.body.style.overflow = "";
}

function resetUpload() {
    // 상태 초기화
    currentFile = null;
    fileInput.value = "";
    uploadArea.classList.remove("hidden");
    previewArea.classList.add("hidden");
    fileProgress.classList.add("hidden");
    uploadProgress.classList.add("hidden");

    // 진행률 초기화
    fileProgressBar.style.width = "0%";
    fileProgressText.textContent = "0%";
    uploadProgressBar.style.width = "0%";
    uploadProgressText.textContent = "0%";

    // 버튼 활성화
    publishVideo.disabled = false;

    // 미리보기 URL 해제
    if (videoPreview.src) {
        URL.revokeObjectURL(videoPreview.src);
        videoPreview.src = "";
        modalVideoPreview.src = "";
    }

    // 폼 초기화
    document.getElementById("video-title").value = "";
    document.getElementById("video-description").value = "";
    document.getElementById("video-hashtags").value = "";
    document.getElementById("video-visibility").value = "public";
    const defaultCommentOption = document.querySelector('input[name="comments-allowed"][value="0"]');
    if (defaultCommentOption) {
        defaultCommentOption.checked = true;
    }
}

// 모달 닫기 이벤트
closeModal.addEventListener("click", () => {
    if (confirm("업로드를 취소하시겠습니까? 입력한 정보가 모두 사라집니다.")) {
        hideUploadModal();
        resetUpload();
    }
});

cancelUpload.addEventListener("click", () => {
    if (confirm("업로드를 취소하시겠습니까? 입력한 정보가 모두 사라집니다.")) {
        hideUploadModal();
        resetUpload();
    }
});

// 업로드 완료 처리
publishVideo.addEventListener("click", async () => {
    const title = document.getElementById("video-title").value.trim();
    const visibility = document.getElementById("video-visibility").value;

    if (!title) {
        alert("동영상 제목을 입력해주세요.");
        return;
    }

    if (!currentFile) {
        alert("업로드할 파일이 없습니다.");
        return;
    }

    // 버튼 비활성화
    publishVideo.disabled = true;
    publishVideo.textContent = "업로드 중...";

    // 서버 업로드 진행률 표시
    uploadProgress.classList.remove("hidden");

    try {
        // 동영상 파일과 메타데이터를 함께 서버로 전송
        const videoData = {
            title: title,
            description: document.getElementById("video-description").value.trim(),
            hashtags: document.getElementById("video-hashtags").value.trim(),
            visibility: visibility,
            commentsAllowed: document.querySelector('input[name="comments-allowed"]:checked').value
        };

        const result = await uploadToServer(currentFile, videoData);
        console.log("업로드 성공:", result);

        alert("동영상이 성공적으로 업로드되었습니다!");
        hideUploadModal();
        resetUpload();

        publishVideo.textContent = "업로드";
    } catch (error) {
        console.error("업로드 오류:", error);
        alert("업로드 중 오류가 발생했습니다: " + error.message);

        // 버튼 재활성화
        publishVideo.disabled = false;
        publishVideo.textContent = "업로드";
        uploadProgress.classList.add("hidden");
    }
});

// 모달 외부 클릭 시 닫기
uploadModal.addEventListener("click", (e) => {
    if (e.target === uploadModal) {
        if (confirm("업로드를 취소하시겠습니까? 입력한 정보가 모두 사라집니다.")) {
            hideUploadModal();
            resetUpload();
        }
    }
});

// ESC 키로 모달 닫기
document.addEventListener("keydown", (e) => {
    if (e.key === "Escape" && !uploadModal.classList.contains("hidden")) {
        if (confirm("업로드를 취소하시겠습니까? 입력한 정보가 모두 사라집니다.")) {
            hideUploadModal();
            resetUpload();
        }
    }
});
const dropzone = document.getElementById("dropzone");
const fileInput = document.getElementById("fileInput");
const selectBtn = document.getElementById("selectFileBtn");
const uploadArea = document.getElementById("upload-area");
const previewArea = document.getElementById("preview-area");
const videoPreview = document.getElementById("video-preview");
const fileInfo = document.getElementById("file-info");
const uploadProgress = document.getElementById("upload-progress");
const progressBar = document.getElementById("progress-bar");
const progressText = document.getElementById("progress-text");
const uploadModal = document.getElementById("upload-modal");
const modalVideoPreview = document.getElementById("modal-video-preview");
const closeModal = document.getElementById("close-modal");
const cancelUpload = document.getElementById("cancel-upload");
const publishVideo = document.getElementById("publish-video");

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

    // 파일 크기 검사 (150MB)
    const maxSize = 1024 * 1024 * 150;
    if (file.size > maxSize) {
        alert('파일 크기가 150MB를 초과합니다.');
        return;
    }

    showPreview(file);
    // 실제 서버 업로드를 원한다면 startUploadWithRealProgress() 사용
    startUploadWithRealProgress();
}

function showPreview(file) {
    // 업로드 영역 숨기기
    uploadArea.classList.add("hidden");

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

async function startUpload() {
    const file = fileInput.files[0];
    if (!file) return;

    // 진행률 표시
    uploadProgress.classList.remove("hidden");

    try {
        const formData = new FormData();
        formData.append('video', file);

        const response = await fetch('/api/upload/video', {
            method: 'POST',
            body: formData,
            // XMLHttpRequest를 사용하여 업로드 진행률 추적
        });

        if (response.ok) {
            const result = await response.json();
            console.log('업로드 성공:', result);

            // 업로드 완료 시 모달 표시
            setTimeout(() => {
                showUploadModal();
            }, 500);
        } else {
            throw new Error(`업로드 실패: ${response.status}`);
        }

    } catch (error) {
        console.error('업로드 오류:', error);
        alert('업로드 중 오류가 발생했습니다. 다시 시도해주세요.');
        resetUpload();
    }
}

// XMLHttpRequest를 사용한 실제 진행률 추적 업로드 함수
async function uploadWithProgress(file) {
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        const formData = new FormData();
        formData.append('video', file);

        // 업로드 진행률 추적
        xhr.upload.addEventListener('progress', (e) => {
            if (e.lengthComputable) {
                const percent = Math.round((e.loaded / e.total) * 100);
                progressBar.style.width = percent + '%';
                progressText.textContent = percent + '%';
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

// 수정된 startUpload 함수 (실제 진행률 추적 사용)
async function startUploadWithRealProgress() {
    const file = fileInput.files[0];
    if (!file) return;

    // 진행률 표시
    uploadProgress.classList.remove("hidden");

    try {
        const result = await uploadWithProgress(file);
        console.log('업로드 성공:', result);

        // 업로드 완료 시 모달 표시
        setTimeout(() => {
            showUploadModal();
        }, 500);

    } catch (error) {
        console.error('업로드 오류:', error);
        alert('업로드 중 오류가 발생했습니다: ' + error.message);
        resetUpload();
    }
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
    fileInput.value = "";
    uploadArea.classList.remove("hidden");
    previewArea.classList.add("hidden");
    uploadProgress.classList.add("hidden");
    progressBar.style.width = "0%";
    progressText.textContent = "0%";

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
    document.querySelector('input[name="comments-allowed"][value="all"]').checked = true;
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

    try {
        // 동영상 메타데이터 저장
        const videoData = {
            title: title,
            description: document.getElementById("video-description").value.trim(),
            hashtags: document.getElementById("video-hashtags").value.trim(),
            visibility: visibility,
            commentsAllowed: document.querySelector('input[name="comments-allowed"]:checked').value
        };

        const response = await fetch('/api/video/metadata', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(videoData)
        });

        if (response.ok) {
            const result = await response.json();
            console.log("메타데이터 저장 성공:", result);
            alert("동영상이 성공적으로 업로드되었습니다!");

            hideUploadModal();
            resetUpload();
        } else {
            throw new Error(`메타데이터 저장 실패: ${response.status}`);
        }

    } catch (error) {
        console.error("메타데이터 저장 오류:", error);
        alert("동영상 정보 저장 중 오류가 발생했습니다: " + error.message);
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
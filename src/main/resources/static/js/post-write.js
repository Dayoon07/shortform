// 글자수 카운터
function updateCharCounter(input, counter, maxLength) {
    const currentLength = input.value.length;
    counter.textContent = `${currentLength}/${maxLength}`;

    if (currentLength > maxLength * 0.9) {
        counter.classList.add('danger');
        counter.classList.remove('warning');
    } else if (currentLength > maxLength * 0.7) {
        counter.classList.add('warning');
        counter.classList.remove('danger');
    } else {
        counter.classList.remove('warning', 'danger');
    }
}

const titleInput = document.getElementById('title');
const titleCounter = document.getElementById('titleCounter');
const contentInput = document.getElementById('content');
const contentCounter = document.getElementById('contentCounter');

titleInput.addEventListener('input', () => updateCharCounter(titleInput, titleCounter, 100));
contentInput.addEventListener('input', () => updateCharCounter(contentInput, contentCounter, 2000));

// 이미지 업로드 기능
const dropArea = document.getElementById('dropArea');
const imageInput = document.getElementById('imageInput');
const imagePreview = document.getElementById('imagePreview');
let uploadedImages = [];

dropArea.addEventListener('click', () => imageInput.click());

dropArea.addEventListener('dragover', (e) => {
    e.preventDefault();
    dropArea.classList.add('drag-over');
});

dropArea.addEventListener('dragleave', () => {
    dropArea.classList.remove('drag-over');
});

dropArea.addEventListener('drop', (e) => {
    e.preventDefault();
    dropArea.classList.remove('drag-over');
    const files = e.dataTransfer.files;
    handleFiles(files);
});

imageInput.addEventListener('change', (e) => {
    handleFiles(e.target.files);
});

function handleFiles(files) {
    for (let file of files) {
        if (uploadedImages.length >= 5) {
            showNotification('최대 5장까지만 업로드할 수 있습니다.', 'warning');
            break;
        }

        if (file.type.startsWith('image/') && file.size <= 5 * 1024 * 1024) {
            uploadedImages.push(file);
            displayImage(file, uploadedImages.length - 1);
        } else {
            showNotification('5MB 이하의 이미지 파일만 업로드 가능합니다.', 'error');
        }
    }
}

function displayImage(file, index) {
    const reader = new FileReader();
    reader.onload = (e) => {
        const imageDiv = document.createElement('div');
        imageDiv.className = 'image-preview';
        imageDiv.innerHTML = `
            <img src="${e.target.result}" class="w-full h-24 object-cover rounded-lg border border-gray-600">
            <button type="button" class="remove-image" onclick="removeImage(${index})">×</button>
        `;
        imagePreview.appendChild(imageDiv);
    };
    reader.readAsDataURL(file);
}

function removeImage(index) {
    uploadedImages.splice(index, 1);
    imagePreview.innerHTML = '';
    uploadedImages.forEach((file, i) => displayImage(file, i));
}

// 폼 제출 처리 - REST API
const form = document.querySelector("form[id='post-submit-fuck']");
const submitBtn = form.querySelector('button[type="submit"]');
const originalBtnText = submitBtn.innerHTML;

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    submitBtn.disabled = true;
    submitBtn.innerHTML = `게시 중...`;

    const visibility = document.querySelector('input[name="visibility"]:checked').value;

    try {
        const formData = new FormData();
        formData.append('title', titleInput.value.trim());
        formData.append('content', contentInput.value.trim());
        formData.append('visibility', visibility);
        uploadedImages.forEach(file => formData.append('images', file));

        const res = await fetch(`${location.origin}/api/post/write`, {
            method: "POST",
            body: formData
        });

        if (res.ok) {
            const data = await res.json();
            console.log(data);

            if (data.success) {
                showNotification(data.message, 'success');
                setTimeout(() => window.location.href = '/community', 1500);
            } else {
                showNotification(data.message || '게시글 작성 실패', 'error');
            }
        }

    } catch (err) {
        console.error(err);
        showNotification('네트워크 오류 발생', 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = '게시하기';
    }
});


// 알림 메시지 표시
function showNotification(message, type = 'info') {
    // 기존 알림이 있으면 제거
    const existingNotification = document.querySelector('.notification');
    if (existingNotification) {
        existingNotification.remove();
    }

    const notification = document.createElement('div');
    notification.className = `notification fixed top-4 right-4 z-50 px-6 py-4 rounded-lg shadow-lg max-w-sm transform transition-all duration-300 translate-x-full`;

    // 타입별 스타일
    const styles = {
        success: 'bg-green-600 text-white border border-green-500',
        error: 'bg-red-600 text-white border border-red-500',
        warning: 'bg-yellow-600 text-white border border-yellow-500',
        info: 'bg-blue-600 text-white border border-blue-500'
    };

    // 타입별 아이콘
    const icons = {
        success: '✅',
        error: '❌',
        warning: '⚠️',
        info: 'ℹ️'
    };

    notification.className += ` ${styles[type] || styles.info}`;
    notification.innerHTML = `
        <div class="flex items-start">
            <span class="mr-2 text-lg">${icons[type] || icons.info}</span>
            <div>
                <p class="font-medium">${message}</p>
            </div>
            <button onclick="this.parentElement.parentElement.remove()" class="ml-4 text-white hover:text-gray-200">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
            </button>
        </div>
    `;

    document.body.appendChild(notification);

    // 애니메이션으로 표시
    setTimeout(() => {
        notification.classList.remove('translate-x-full');
    }, 100);

    // 자동 제거 (성공/정보 메시지만)
    if (type === 'success' || type === 'info') {
        setTimeout(() => {
            if (notification.parentElement) {
                notification.classList.add('translate-x-full');
                setTimeout(() => {
                    notification.remove();
                }, 300);
            }
        }, 3000);
    }
}

// 페이지 이탈 시 경고 (내용이 있을 때만)
window.addEventListener('beforeunload', (e) => {
    if (titleInput.value.trim() || contentInput.value.trim() || uploadedImages.length > 0) {
        e.preventDefault();
        e.returnValue = '작성 중인 내용이 있습니다. 정말로 페이지를 떠나시겠습니까?';
    }
});

// 입력 필드 실시간 검증
function validateForm() {
    const title = titleInput.value.trim();
    const content = contentInput.value.trim();
    const isValid = title.length > 0 && title.length <= 100 &&
        content.length > 0 && content.length <= 2000;

    submitBtn.disabled = !isValid;

    if (isValid) {
        submitBtn.classList.remove('opacity-50', 'cursor-not-allowed');
    } else {
        submitBtn.classList.add('opacity-50', 'cursor-not-allowed');
    }
}

// 초기 검증
validateForm();
titleInput.addEventListener('input', validateForm);
contentInput.addEventListener('input', validateForm);
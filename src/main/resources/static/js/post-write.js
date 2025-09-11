class PostWriter {
    constructor() {
        this.uploadedImages = [];
        this.maxImages = 5;
        this.maxFileSize = 5 * 1024 * 1024; // 5MB
        this.maxContentLength = 2000;

        this.initializeElements();
        this.bindEvents();
        this.validateForm();
    }

    initializeElements() {
        this.contentInput = document.getElementById('content');
        this.contentCounter = document.getElementById('contentCounter');
        this.dropArea = document.getElementById('dropArea');
        this.imageInput = document.getElementById('imageInput');
        this.imagePreview = document.getElementById('imagePreview');
        this.form = document.querySelector("form[id='post-submit-fuck']");
        this.submitBtn = this.form.querySelector('button[type="submit"]');
        this.originalBtnText = this.submitBtn.innerHTML;
    }

    bindEvents() {
        // 컨텐츠 입력 이벤트
        this.contentInput.addEventListener('input', () => {
            this.updateCharCounter();
            this.validateForm();
        });

        // 이미지 업로드 이벤트
        this.dropArea.addEventListener('click', () => this.imageInput.click());
        this.dropArea.addEventListener('dragover', this.handleDragOver.bind(this));
        this.dropArea.addEventListener('dragleave', this.handleDragLeave.bind(this));
        this.dropArea.addEventListener('drop', this.handleDrop.bind(this));
        this.imageInput.addEventListener('change', (e) => this.handleFiles(e.target.files));

        // 폼 제출 이벤트
        this.form.addEventListener('submit', this.handleFormSubmit.bind(this));

        // 페이지 이탈 경고
        window.addEventListener('beforeunload', this.handleBeforeUnload.bind(this));
    }

    updateCharCounter() {
        const currentLength = this.contentInput.value.length;
        this.contentCounter.textContent = `${currentLength}/${this.maxContentLength}`;

        // 클래스 관리 최적화
        this.contentCounter.classList.remove('warning', 'danger');

        if (currentLength > this.maxContentLength * 0.9) {
            this.contentCounter.classList.add('danger');
        } else if (currentLength > this.maxContentLength * 0.7) {
            this.contentCounter.classList.add('warning');
        }
    }

    handleDragOver(e) {
        e.preventDefault();
        this.dropArea.classList.add('drag-over');
    }

    handleDragLeave() {
        this.dropArea.classList.remove('drag-over');
    }

    handleDrop(e) {
        e.preventDefault();
        this.dropArea.classList.remove('drag-over');
        this.handleFiles(e.dataTransfer.files);
    }

    handleFiles(files) {
        const filesArray = Array.from(files);
        const remainingSlots = this.maxImages - this.uploadedImages.length;

        if (remainingSlots <= 0) {
            this.showNotification(`최대 ${this.maxImages}장까지만 업로드할 수 있습니다.`, 'warning');
            return;
        }

        const validFiles = filesArray.slice(0, remainingSlots).filter(file => {
            if (!file.type.startsWith('image/')) {
                this.showNotification('이미지 파일만 업로드 가능합니다.', 'error');
                return false;
            }
            if (file.size > this.maxFileSize) {
                this.showNotification('5MB 이하의 파일만 업로드 가능합니다.', 'error');
                return false;
            }
            return true;
        });

        validFiles.forEach(file => {
            this.isFormSubmitted = false;
            this.uploadedImages.push(file);
            this.displayImage(file, this.uploadedImages.length - 1);
        });

        this.validateForm();
    }

    displayImage(file, index) {
        const reader = new FileReader();
        reader.onload = (e) => {
            const imageDiv = document.createElement('div');
            imageDiv.className = 'image-preview relative';
            imageDiv.innerHTML = `
                <img src="${e.target.result}" 
                     class="w-full h-24 object-cover rounded-lg border border-gray-600"
                     alt="업로드된 이미지">
                <button type="button" 
                        class="remove-image absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600 transition-colors"
                        data-index="${index}"
                        aria-label="이미지 삭제">×</button>
            `;

            imageDiv.querySelector('.remove-image').addEventListener('click', (e) => {
                this.removeImage(parseInt(e.target.dataset.index));
            });

            this.imagePreview.appendChild(imageDiv);
        };
        reader.readAsDataURL(file);
    }

    removeImage(index) {
        this.uploadedImages.splice(index, 1);
        this.rerenderImages();
        this.validateForm();
    }

    rerenderImages() {
        this.imagePreview.innerHTML = '';
        this.uploadedImages.forEach((file, index) => {
            this.displayImage(file, index);
        });
    }

    async handleFormSubmit(e) {
        e.preventDefault();

        if (this.isSubmitting) return;
        if (!this.validateForm()) return;

        this.isSubmitting = true;
        this.setSubmitButtonLoading(true);

        try {
            const formData = this.createFormData();
            const response = await this.submitPost(formData);

            if (response.success) {
                this.showNotification(response.message, 'success');
                this.resetForm();
                setTimeout(() => {
                    window.location.href = location.origin + '/@' + JSON.parse(localStorage.getItem("user")).mention + '/post'
                }, 1500);
            } else {
                throw new Error(response.message || '게시글 작성에 실패했습니다.');
            }

        } catch (error) {
            console.error('게시글 작성 오류:', error);
            this.showNotification(error.message || '네트워크 오류가 발생했습니다.', 'error');
        } finally {
            this.isSubmitting = false;
            this.setSubmitButtonLoading(false);
        }
    }

    createFormData() {
        const formData = new FormData();
        const visibility = document.querySelector('input[name="visibility"]:checked')?.value || 'public';

        formData.append('content', this.contentInput.value.trim());
        formData.append('visibility', visibility);

        this.uploadedImages.forEach(file => {
            formData.append('images', file);
        });

        return formData;
    }

    async submitPost(formData) {
        const response = await fetch(`${location.origin}/api/post/write`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status}`);
        }

        return await response.json();
    }

    setSubmitButtonLoading(isLoading) {
        this.submitBtn.disabled = isLoading;
        this.submitBtn.innerHTML = isLoading ? '게시 중...' : this.originalBtnText;
    }

    /**
     * 폼 검증 로직 최적화
     * - 글만 있는 경우: 유효
     * - 이미지만 있는 경우: 유효
     * - 글과 이미지 모두 있는 경우: 유효
     * - 글과 이미지 모두 없는 경우: 무효
     */
    validateForm() {
        const content = this.contentInput.value.trim();
        const hasImages = this.uploadedImages.length > 0;
        const contentTooLong = content.length > this.maxContentLength;

        // 검증 조건들
        const hasContent = content.length > 0;
        const hasValidContent = hasContent && !contentTooLong;
        const hasAnyContent = hasValidContent || hasImages;

        // 최종 유효성 검사
        const isValid = hasAnyContent && !this.isSubmitting;

        // UI 업데이트
        this.updateSubmitButton(isValid);
        this.updateValidationMessages(hasContent, hasImages, contentTooLong);

        return isValid;
    }

    /**
     * 제출 버튼 상태 업데이트
     */
    updateSubmitButton(isValid) {
        this.submitBtn.disabled = !isValid;
        this.submitBtn.classList.toggle('opacity-50', !isValid);
        this.submitBtn.classList.toggle('cursor-not-allowed', !isValid);
    }

    /**
     * 검증 메시지 업데이트
     */
    updateValidationMessages(hasContent, hasImages, contentTooLong) {
        // 기존 경고 메시지 제거
        this.removeValidationMessage();

        if (!hasContent && !hasImages) {
            this.showValidationMessage('글 또는 이미지 중 하나를 입력해주세요.', 'warning');
        } else if (contentTooLong) {
            this.showValidationMessage('내용이 너무 깁니다. 2000자 이하로 작성해주세요.', 'error');
        }
    }

    /**
     * 검증 메시지 표시
     */
    showValidationMessage(message, type) {
        const existingMessage = this.form.querySelector('.validation-message');
        if (existingMessage) {
            existingMessage.remove();
        }

        const messageElement = document.createElement('div');
        messageElement.className = `validation-message text-sm mt-2 ${
            type === 'error' ? 'text-red-400' : 'text-yellow-500'
        }`;
        messageElement.textContent = message;

        // 제출 버튼 위에 메시지 삽입
        const buttonContainer = this.form.querySelector('.flex.justify-end');
        buttonContainer.parentNode.insertBefore(messageElement, buttonContainer);
    }

    /**
     * 검증 메시지 제거
     */
    removeValidationMessage() {
        const existingMessage = this.form.querySelector('.validation-message');
        if (existingMessage) {
            existingMessage.remove();
        }
    }

    /**
     * 폼을 초기화합니다.
     */
    resetForm() {
        this.contentInput.value = '';
        this.uploadedImages = [];
        this.imagePreview.innerHTML = '';
        this.updateCharCounter();
        this.removeValidationMessage();
        this.validateForm();

        // 공개범위 라디오 버튼을 기본값으로 리셋
        const defaultVisibility = document.querySelector('input[name="visibility"][value="public"]');
        if (defaultVisibility) {
            defaultVisibility.checked = true;
        }
    }

    /**
     * 페이지 이탈 시 경고 (제출 중이 아닐 때만)
     */
    handleBeforeUnload(e) {
        const hasContent = this.contentInput.value.trim().length > 0;
        const hasImages = this.uploadedImages.length > 0;

        if (!this.isSubmitting && (hasContent || hasImages)) {
            e.preventDefault();
            e.returnValue = '작성 중인 내용이 있습니다. 정말로 페이지를 떠나시겠습니까?';
        }
    }

    showNotification(message, type = 'info') {
        // 기존 알림 제거
        const existingNotification = document.querySelector('.notification');
        if (existingNotification) {
            existingNotification.remove();
        }

        const notification = document.createElement('div');
        const styles = {
            success: 'bg-green-600 text-white border-green-500',
            error: 'bg-red-600 text-white border-red-500',
            warning: 'bg-yellow-600 text-white border-yellow-500',
            info: 'bg-blue-600 text-white border-blue-500'
        };

        const icons = {
            success: '✅',
            error: '❌',
            warning: '⚠️',
            info: 'ℹ️'
        };

        notification.className = `notification fixed top-4 right-4 z-50 px-6 py-4 rounded-lg shadow-lg max-w-sm transform transition-all duration-300 translate-x-full border ${styles[type] || styles.info}`;

        notification.innerHTML = `
            <div class="flex items-start">
                <span class="mr-2 text-lg">${icons[type] || icons.info}</span>
                <div class="flex-1">
                    <p class="font-medium">${message}</p>
                </div>
                <button class="ml-4 text-white hover:text-gray-200 transition-colors" 
                        onclick="this.closest('.notification').remove()"
                        aria-label="알림 닫기">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                    </svg>
                </button>
            </div>
        `;

        document.body.appendChild(notification);

        // 애니메이션으로 표시
        requestAnimationFrame(() => {
            notification.classList.remove('translate-x-full');
        });

        // 자동 제거 (성공/정보 메시지만)
        if (type === 'success' || type === 'info') {
            setTimeout(() => {
                if (notification.parentElement) {
                    notification.classList.add('translate-x-full');
                    setTimeout(() => notification.remove(), 300);
                }
            }, 3000);
        }
    }
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    new PostWriter();
});
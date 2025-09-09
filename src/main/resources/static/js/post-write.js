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
            alert('최대 5장까지만 업로드할 수 있습니다.');
            break;
        }

        if (file.type.startsWith('image/') && file.size <= 5 * 1024 * 1024) {
            uploadedImages.push(file);
            displayImage(file, uploadedImages.length - 1);
        } else {
            alert('5MB 이하의 이미지 파일만 업로드 가능합니다.');
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

// 태그 기능
const tagInput = document.getElementById('tagInput');
const addTagBtn = document.getElementById('addTagBtn');
const tagContainer = document.getElementById('tagContainer');
const tagsHidden = document.getElementById('tagsHidden');
let tags = [];

function addTag() {
    const tagValue = tagInput.value.trim();
    if (tagValue && tags.length < 5 && !tags.includes(tagValue)) {
        tags.push(tagValue);
        updateTagDisplay();
        updateHiddenTags();
        tagInput.value = '';
    } else if (tags.length >= 5) {
        alert('최대 5개의 태그만 추가할 수 있습니다.');
    } else if (tags.includes(tagValue)) {
        alert('이미 추가된 태그입니다.');
    }
}

function removeTag(index) {
    tags.splice(index, 1);
    updateTagDisplay();
    updateHiddenTags();
}

function updateTagDisplay() {
    tagContainer.innerHTML = tags.map((tag, index) => `
                    <span class="bg-blue-600 text-white px-3 py-1 rounded-full text-sm flex items-center space-x-2">
                        <span>#${tag}</span>
                        <button type="button" onclick="removeTag(${index})" class="hover:bg-blue-700 rounded-full w-4 h-4 flex items-center justify-center text-xs">×</button>
                    </span>
                `).join('');
}

function updateHiddenTags() {
    tagsHidden.value = tags.join(',');
}

addTagBtn.addEventListener('click', addTag);
tagInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        e.preventDefault();
        addTag();
    }
});

// 폼 제출 전 이미지 파일 추가
document.querySelector('form').addEventListener('submit', (e) => {
    // 기존 file input 제거
    const existingFiles = document.querySelectorAll('input[name="images"]');
    existingFiles.forEach(input => {
        if (input !== imageInput) input.remove();
    });

    // 업로드된 이미지들을 FormData에 추가하기 위해 새로운 file input 생성
    uploadedImages.forEach((file, index) => {
        const fileInput = document.createElement('input');
        fileInput.type = 'file';
        fileInput.name = 'images';
        fileInput.style.display = 'none';

        // Create new FileList with the file
        const dataTransfer = new DataTransfer();
        dataTransfer.items.add(file);
        fileInput.files = dataTransfer.files;

        document.querySelector('form').appendChild(fileInput);
    });
});
"use strict";

document.addEventListener("DOMContentLoaded", () => {

    // 모든 태그 요소들을 가져오기
    const tags = document.querySelectorAll('.tag');

    // 각 태그에 클릭 이벤트 리스너 추가
    tags.forEach(tag => {
        tag.addEventListener('click', function() {
            // 모든 태그에서 active 클래스 제거
            tags.forEach(t => t.classList.remove('active'));

            // 클릭된 태그에 active 클래스 추가
            this.classList.add('active');

            // 선택된 태그 값 가져오기 (필요시 사용)
            const selectedTag = this.getAttribute('data-tag');
            console.log('선택된 태그:', selectedTag);

            // 여기서 필요한 추가 로직 실행 가능
            // 예: API 호출, 필터링 등
        });
    });

});
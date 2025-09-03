"use strict";

document.addEventListener("DOMContentLoaded", () => {

    const tags = document.querySelectorAll('.tag');

    tags.forEach(tag => {
        tag.addEventListener('click', async function (){
            tags.forEach(t => t.classList.remove('active'));

            this.classList.add('active');
            const selectedTag = this.getAttribute('data-tag');
            console.log('선택된 태그:', selectedTag);

            try {
                const res = await fetch(`${location.origin}/api/videos/tag?hashtag=${selectedTag}`, {
                    method: "POST"
                });

                if (res.ok) {
                    const data = await res.json();
                    console.log(data);

                    if (data.length > 0) {
                        document.getElementById("aaaaaaaa").classList.add("grid");
                        data.forEach(video => {
                            document.getElementById("aaaaaaaa").innerHTML = `
                                <div>
                                    <a href="${location.origin}/@${video.mention}/swipe/video/${video.videoLoc}" class="relative group cursor-pointer">
                                        <div class="relative aspect-[9/16] bg-gray-900 rounded-lg overflow-hidden">
                                            <video src="${location.origin + video.videoSrc}" data-src="${video.videoSrc}" playsinline muted class="lazy-video w-full h-full object-cover"></video>
                                            <div class="absolute inset-0 bg-black/20 opacity-0 group-hover:opacity-100 transition-opacity duration-200"></div>
                
                                            <div class="absolute bottom-2 left-2 flex items-center space-x-1 bg-black/60 rounded-full px-2 py-1 backdrop-blur-sm">
                                                <svg class="w-3 h-3 text-white" fill="currentColor" viewBox="0 0 24 24">
                                                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                                                </svg>
                                                <span class="text-white text-xs font-medium">${video.likeCount}</span>
                                            </div>
                
                                            <div class="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                                                <div class="w-12 h-12 bg-white/20 rounded-full flex items-center justify-center backdrop-blur-sm border border-white/30">
                                                    <svg class="w-6 h-6 text-white ml-1" fill="currentColor" viewBox="0 0 24 24">
                                                        <path d="M8 5v14l11-7z"/>
                                                    </svg>
                                                </div>
                                            </div>
                                        </div>
                                    </a>
                                    <div class="mt-2 px-1">
                                        <a href="${location.origin}/@${video.mention}" class="block flex items-center space-x-2 mb-1">
                                            <img src="${video.profileImgSrc}" class="w-8 h-8 rounded-full object-cover" alt="프로필">
                                            <span class="text-white text-md font-semibold truncate">${video.uploaderUsername}</span>
                                        </a>
                
                                        <p class="text-gray-300 text-md leading-tight line-clamp-2 mb-1">
                                            ${video.videoTitle.length > 25 ? video.videoTitle.substring(0, 25) + '...' : video.videoTitle}
                                        </p>
    
                                        <span class="text-gray-400 text-sm">조회수 ${video.videoViews}회</span>
                                    </div>
                                </div>
                            `;
                        });
                    } else {
                        document.getElementById("aaaaaaaa").classList.remove("grid");
                        document.getElementById("aaaaaaaa").innerHTML = `
                            <div class="w-full">
                                <h1 class="text-3xl text-center font-semibold w-full md:w-[500px]">해시태그와 관련된 영상이 없습니다.</h1>
                            </div>
                        `;
                    }
                }

            } catch (error) {
                console.log(error);
            }

        });
    });

});
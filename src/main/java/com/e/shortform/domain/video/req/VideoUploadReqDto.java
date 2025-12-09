package com.e.shortform.domain.video.req;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class VideoUploadReqDto {

    private MultipartFile video;
    private String title;
    private String description;
    private String hashtags;
    private String visibility;
    private String commentsAllowed;
    private MultipartFile thumbnail;

}

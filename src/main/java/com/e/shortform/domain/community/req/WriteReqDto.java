package com.e.shortform.domain.community.req;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class WriteReqDto {

    private String content;
    private String visibility;
    private List<MultipartFile> images;

}

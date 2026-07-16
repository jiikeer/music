package com.music.model.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PostRequest {
    private Integer id;
    private Integer userId;
    private String title;
    private String content;
    private MultipartFile coverFile;
}

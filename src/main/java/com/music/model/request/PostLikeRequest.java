package com.music.model.request;
import lombok.Data;

@Data
public class PostLikeRequest {
    private Integer userId;
    private Integer postId;
}
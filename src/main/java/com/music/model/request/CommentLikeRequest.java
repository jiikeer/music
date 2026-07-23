package com.music.model.request;

import lombok.Data;

@Data
public class CommentLikeRequest {
    private Integer userId;
    private Integer commentId;
    /** "song" or "post" */
    private String commentType;
}

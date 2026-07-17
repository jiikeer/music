package com.music.model.request;
import lombok.Data;

@Data
public class CommentRequest {
    // 评论目标ID
    private Integer targetId;
    // 评论用户
    private Integer userId;
    private String content;
    // 0=一级评论，大于0为回复某条评论id
    private Integer parentId;
}

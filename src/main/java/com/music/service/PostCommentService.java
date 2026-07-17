package com.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.music.common.R;
import com.music.model.domain.PostComment;
import com.music.model.request.CommentRequest;

public interface PostCommentService extends IService<PostComment> {
    // 新增帖子评论/回复
    R addPostComment(CommentRequest request);
    // 删除自己帖子评论
    R deletePostComment(Integer commentId, Integer loginUserId);
    // 查询帖子全部评论（带用户昵称头像）
    R listPostComment(Integer postId);
}
package com.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.music.common.R;
import com.music.model.domain.CommentLike;
import com.music.model.request.CommentLikeRequest;

public interface CommentLikeService extends IService<CommentLike> {
    /** 点赞/取消点赞评论 toggle */
    R likeComment(CommentLikeRequest request);
    /** 获取用户点赞过的评论列表（带来源信息） */
    R getUserLikedComments(Integer userId);
}

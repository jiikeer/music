package com.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.music.common.R;
import com.music.model.domain.PostSupport;
import com.music.model.request.PostLikeRequest;

public interface PostSupportService extends IService<PostSupport> {
    // 点赞/取消帖子点赞
    R likePost(PostLikeRequest request);
    // 判断用户是否点赞该帖子
    Boolean checkPostLike(Integer userId, Integer postId);
}
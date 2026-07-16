package com.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.music.common.R;
import com.music.model.domain.Post;
import com.music.model.request.PostRequest;

public interface PostService extends IService<Post> {

    R publishPost(PostRequest request);

    R updatePost(PostRequest request);

    R deletePost(Integer postId, Integer loginUserId);

    R postDetail(Integer postId);

    R pageAllPassPost(Integer page, Integer size);

    R listUserPost(Integer userId);

    R adminPagePost(Integer page,Integer size,Integer status);

    R adminDeletePost(Integer postId);

    R auditPost(Integer postId,Integer status,String auditReason);

}

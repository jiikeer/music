package com.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.music.common.R;
import com.music.mapper.PostMapper;
import com.music.mapper.PostSupportMapper;
import com.music.model.domain.Post;
import com.music.model.domain.PostSupport;
import com.music.model.request.PostLikeRequest;
import com.music.service.PostSupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class PostSupportServiceImpl extends ServiceImpl<PostSupportMapper, PostSupport> implements PostSupportService {
    private final PostSupportMapper supportMapper;
    private final PostMapper postMapper;

    @Override
    @Transactional
    public R likePost(PostLikeRequest request) {
        Integer userId = request.getUserId();
        Integer postId = request.getPostId();
        QueryWrapper<PostSupport> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("post_id", postId);
        PostSupport record = getOne(wrapper);
        Post post = postMapper.selectById(postId);
        if (post == null) return R.error("帖子不存在");

        if (record == null) {
            // 点赞新增记录
            PostSupport support = new PostSupport();
            support.setUserId(userId);
            support.setPostId(postId);
            support.setCreateTime(new Date());
            save(support);
            post.setLikeCount(post.getLikeCount() + 1);
        } else {
            // 取消点赞删除记录
            remove(wrapper);
            post.setLikeCount(post.getLikeCount() - 1);
        }
        postMapper.updateById(post);
        return R.success("操作成功");
    }

    @Override
    public Boolean checkPostLike(Integer userId, Integer postId) {
        QueryWrapper<PostSupport> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("post_id", postId);
        return count(wrapper) > 0;
    }
}
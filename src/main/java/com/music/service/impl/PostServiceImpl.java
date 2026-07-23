package com.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.music.common.R;
import com.music.config.FileUploadConfig;
import com.music.mapper.PostMapper;
import com.music.mapper.UserMapper;
import com.music.model.domain.Post;
import com.music.model.domain.User;
import com.music.model.request.PostRequest;
import com.music.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final FileUploadConfig uploadUtil;

    // 查询用户自己全部帖子，填充 username
    @Override
    public R listUserPost(Integer userId) {
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("create_time");
        List<Post> list = postMapper.selectList(wrapper);
        // 填充 username（用户自己的帖子，username 都是同一个，查一次即可）
        if (!list.isEmpty()) {
            User u = userMapper.selectById(userId);
            if (u != null) {
                list.forEach(p -> p.setUsername(u.getUsername()));
            }
        }
        return R.success("查询成功", list);
    }
    @Override
    @Transactional
    public R publishPost(PostRequest request) {
        Post post = new Post();
        BeanUtils.copyProperties(request, post);
        MultipartFile coverFile = request.getCoverFile();
        try {
            if (coverFile != null && !coverFile.isEmpty()) {
                String coverUrl = uploadUtil.upload(coverFile, "post");
                post.setCover(coverUrl);
            } else {
                post.setCover("/post/default.jpg");
            }
            post.setCreateTime(new Date());
            post.setUpdateTime(new Date());
            post.setStatus(0);
            post.setLikeCount(0);
            post.setCommentCount(0);
            int row = postMapper.insert(post);
            return row > 0 ? R.success("发布成功，等待管理员审核") : R.error("发布失败");
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("发布异常：" + e.getMessage());
        }
    }

    // 修改帖子
    @Override
    @Transactional
    public R updatePost(PostRequest request) {
        Integer postId = request.getId();
        if (postId == null) return R.error("帖子id不能为空");
        Post oldPost = postMapper.selectById(postId);
        if (oldPost == null) return R.error("帖子不存在");
        // 复制参数，不覆盖id、发布人userId
        BeanUtils.copyProperties(request, oldPost, "id", "userId");
        MultipartFile newCover = request.getCoverFile();
        try {
            // 替换新封面
            if (newCover != null && !newCover.isEmpty()) {
                String newCoverUrl = uploadUtil.upload(newCover, "post");
                oldPost.setCover(newCoverUrl);
            }
            oldPost.setUpdateTime(new Date());
            int row = postMapper.updateById(oldPost);
            return row > 0 ? R.success("修改成功") : R.error("未修改任何内容");
        } catch (Exception e) {
            return R.error("修改异常：" + e.getMessage());
        }
    }

    // 删除帖子
    @Override
    public R deletePost(Integer postId, Integer loginUserId) {
        Post post = postMapper.selectById(postId);
        if (post == null) return R.error("帖子不存在");
        if (!post.getUserId().equals(loginUserId)) {
            return R.error("无权限删除他人帖子");
        }
        int row = postMapper.deleteById(postId);
        return row > 0 ? R.success("删除成功") : R.error("删除失败");
    }

    // 帖子详情
    @Override
    public R postDetail(Integer postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) return R.error("帖子不存在");
        return R.success("查询成功", post);
    }

    // 分页查询已通过帖子（status=1）
    @Override
    public R pageAllPassPost(Integer page, Integer size) {
        Page<Post> pageInfo = new Page<>(page, size);
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByDesc("create_time");
        postMapper.selectPage(pageInfo, wrapper);

        // 批量填充 username
        List<Post> records = pageInfo.getRecords();
        Set<Integer> userIds = records.stream()
                .map(Post::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(HashSet::new));
        if (!userIds.isEmpty()) {
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.in("id", userIds);
            Map<Integer, User> userMap = userMapper.selectList(userWrapper).stream()
                    .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));
            records.forEach(p -> {
                User u = userMap.get(p.getUserId());
                if (u != null) {
                    p.setUsername(u.getUsername());
                    p.setAvatar(u.getAvatar());
                }
            });
        }

        return R.success("查询成功", pageInfo);
    }

    @Override
    public R adminPagePost(Integer page, Integer size, Integer status) {
        Page<Post> pageInfo = new Page<>(page, size);
        QueryWrapper<Post> wrapper = new QueryWrapper<>();
        if(status != null){
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        postMapper.selectPage(pageInfo, wrapper);

        // 批量填充 username + avatar
        List<Post> records2 = pageInfo.getRecords();
        Set<Integer> userIds2 = records2.stream()
                .map(Post::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(HashSet::new));
        if (!userIds2.isEmpty()) {
            QueryWrapper<User> userWrapper2 = new QueryWrapper<>();
            userWrapper2.in("id", userIds2);
            Map<Integer, User> userMap2 = userMapper.selectList(userWrapper2).stream()
                    .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));
            records2.forEach(p -> {
                User u = userMap2.get(p.getUserId());
                if (u != null) {
                    p.setUsername(u.getUsername());
                    p.setAvatar(u.getAvatar());
                }
            });
        }

        return R.success("查询成功", pageInfo);
    }

    @Override
    public R adminDeletePost(Integer postId) {
        Post post = postMapper.selectById(postId);
        if(post == null) return R.error("帖子不存在");
        int row = postMapper.deleteById(postId);
        return row > 0 ? R.success("管理员删除帖子成功") : R.error("删除失败");
    }

    @Override
    public R auditPost(Integer postId, Integer status, String auditReason) {
        // 校验帖子是否存在
        Post post = postMapper.selectById(postId);
        if (post == null) {
            return R.error("不存在该帖子");
        }
        // 限制合法审核状态
        if (status != 1 && status != 2) {
            return R.error("审核状态只能为1(通过)、2(驳回)");
        }
        // 封装更新字段
        Post updatePost = new Post();
        updatePost.setId(postId);
        updatePost.setStatus(status);
        updatePost.setAuditReason(auditReason);
        updatePost.setUpdateTime(new Date());
        // 更新数据库
        int row = postMapper.updateById(updatePost);
        if (row > 0) {
            return R.success("帖子审核完成");
        } else {
            return R.error("审核操作未生效");
        }
    }

}
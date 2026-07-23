package com.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.music.common.R;
import com.music.mapper.CommentLikeMapper;
import com.music.mapper.PostCommentMapper;
import com.music.mapper.UserMapper;
import com.music.model.domain.CommentLike;
import com.music.model.domain.PostComment;
import com.music.model.domain.User;
import com.music.model.request.CommentRequest;
import com.music.service.PostCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl extends ServiceImpl<PostCommentMapper, PostComment> implements PostCommentService {

    private final PostCommentMapper postCommentMapper;
    private final UserMapper userMapper;
    private final CommentLikeMapper commentLikeMapper;

    /**
     * 新增帖子评论/回复评论
     */
    @Override
    public R addPostComment(CommentRequest request) {
        PostComment comment = new PostComment();
        BeanUtils.copyProperties(request, comment);
        // targetId 前端传帖子id，赋值给postId
        comment.setPostId(request.getTargetId());
        comment.setCreateTime(new Date());
        comment.setLikeCount(0);
        // parentId不传默认0（一级评论）
        comment.setParentId(request.getParentId() == null ? 0 : request.getParentId());
        postCommentMapper.insert(comment);
        return R.success("评论发布成功");
    }

    /**
     * 删除帖子评论，仅评论作者可删
     */
    @Override
    public R deletePostComment(Integer commentId, Integer loginUserId) {
        PostComment comment = getById(commentId);
        if (comment == null) {
            return R.error("评论不存在");
        }
        // 判断登录用户是否为评论发布人
        if (!comment.getUserId().equals(loginUserId)) {
            return R.error("仅能删除自己发布的评论");
        }
        removeById(commentId);
        return R.success("删除评论成功");
    }

    @Override
    public R listPostComment(Integer postId) {
        return listPostComment(postId, null);
    }

    /**
     * 根据帖子id查询所有评论，附带评论者用户名、头像、是否已点赞
     */
    @Override
    public R listPostComment(Integer postId, Integer userId) {
        QueryWrapper<PostComment> wrapper = new QueryWrapper<>();
        wrapper.eq("post_id", postId).orderByDesc("create_time");
        List<PostComment> commentList = postCommentMapper.selectList(wrapper);
        return R.success(null, buildCommentWithUser(commentList, userId));
    }

    /**
     * 封装评论+用户信息，和SongComment工具方法逻辑完全一致，避免N+1查询
     */
    private List<Map<String, Object>> buildCommentWithUser(List<PostComment> comments, Integer userId) {
        if (comments == null || comments.isEmpty()) {
            return Collections.emptyList();
        }
        // 收集所有评论的用户id
        Set<Integer> userIds = comments.stream()
                .map(PostComment::getUserId)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(HashSet::new));

        Map<Integer, User> userMap = new LinkedHashMap<>();
        if (!userIds.isEmpty()) {
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.in("id", userIds);
            userMap = userMapper.selectList(userWrapper).stream()
                    .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        }

        // 查询当前用户对这批评论的点赞状态
        Set<Integer> likedCommentIds = new HashSet<>();
        if (userId != null && !comments.isEmpty()) {
            List<Integer> commentIds = comments.stream()
                    .map(PostComment::getId)
                    .collect(Collectors.toList());
            QueryWrapper<CommentLike> likeWrapper = new QueryWrapper<>();
            likeWrapper.eq("user_id", userId)
                    .eq("comment_type", "post")
                    .in("comment_id", commentIds);
            List<CommentLike> likes = commentLikeMapper.selectList(likeWrapper);
            likedCommentIds = likes.stream()
                    .map(CommentLike::getCommentId)
                    .collect(Collectors.toCollection(HashSet::new));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (PostComment comment : comments) {
            User user = userMap.get(comment.getUserId());
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", comment.getId());
            map.put("postId", comment.getPostId());
            map.put("userId", comment.getUserId());
            map.put("content", comment.getContent());
            map.put("parentId", comment.getParentId());
            map.put("likeCount", comment.getLikeCount());
            map.put("createTime", comment.getCreateTime());
            // 用户信息
            map.put("username", user != null ? user.getUsername() : "");
            map.put("avatar", user != null ? user.getAvatar() : "");
            map.put("isLiked", likedCommentIds.contains(comment.getId()));
            result.add(map);
        }
        return result;
    }
}

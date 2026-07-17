package com.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.music.common.R;
import com.music.mapper.SongCommentMapper;
import com.music.mapper.UserMapper;
import com.music.model.domain.SongComment;
import com.music.model.domain.User;
import com.music.model.request.CommentRequest;
import com.music.service.SongCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongCommentServiceImpl extends ServiceImpl<SongCommentMapper, SongComment> implements SongCommentService {
    private final SongCommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    public R addSongComment(CommentRequest request) {
        SongComment comment = new SongComment();
        BeanUtils.copyProperties(request, comment);
        comment.setSongId(request.getTargetId());
        comment.setCreateTime(new Date());
        comment.setLikeCount(0);
        comment.setParentId(request.getParentId() == null ? 0 : request.getParentId());
        commentMapper.insert(comment);
        return R.success("评论发布成功");
    }

    @Override
    public R deleteSongComment(Integer commentId, Integer loginUserId) {
        SongComment comment = getById(commentId);
        if (comment == null) return R.error("评论不存在");
        if (!comment.getUserId().equals(loginUserId)) return R.error("仅可删除自己的评论");
        removeById(commentId);
        return R.success("删除评论成功");
    }

    @Override
    public R listSongComment(Integer songId) {
        QueryWrapper<SongComment> wrapper = new QueryWrapper<>();
        wrapper.eq("song_id", songId).orderByDesc("create_time");
        List<SongComment> commentList = commentMapper.selectList(wrapper);
        return R.success(null, buildCommentWithUser(commentList));
    }

    // 拼接评论+发布用户信息，和你原有评论工具方法一致
    private List<Map<String, Object>> buildCommentWithUser(List<SongComment> comments) {
        if (comments.isEmpty()) return Collections.emptyList();
        Set<Integer> userIds = comments.stream()
                .map(SongComment::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(HashSet::new));
        Map<Integer, User> userMap = new LinkedHashMap<>();
        if (!userIds.isEmpty()) {
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.in("id", userIds);
            userMap = userMapper.selectList(userWrapper).stream()
                    .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (SongComment c : comments) {
            User u = userMap.get(c.getUserId());
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("songId", c.getSongId());
            map.put("userId", c.getUserId());
            map.put("content", c.getContent());
            map.put("parentId", c.getParentId());
            map.put("likeCount", c.getLikeCount());
            map.put("createTime", c.getCreateTime());
            map.put("username", u != null ? u.getUsername() : "");
            map.put("avatar", u != null ? u.getAvatar() : "");
            result.add(map);
        }
        return result;
    }
}
package com.music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.music.common.R;
import com.music.mapper.PostCommentMapper;
import com.music.mapper.SongCommentMapper;
import com.music.model.domain.PostComment;
import com.music.model.domain.SongComment;
import com.music.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final SongCommentMapper songCommentMapper;
    private final PostCommentMapper postCommentMapper;
    private final CommentLikeService commentLikeService;

    @GetMapping("")
    public R all() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<SongComment> songComments = songCommentMapper.selectList(null);
        for (SongComment sc : songComments) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", sc.getId());
            m.put("type", "song");
            m.put("userId", sc.getUserId());
            m.put("content", sc.getContent());
            m.put("createTime", sc.getCreateTime());
            result.add(m);
        }
        List<PostComment> postComments = postCommentMapper.selectList(null);
        for (PostComment pc : postComments) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", pc.getId());
            m.put("type", "post");
            m.put("userId", pc.getUserId());
            m.put("content", pc.getContent());
            m.put("createTime", pc.getCreateTime());
            result.add(m);
        }
        return R.success(null, result);
    }

    @GetMapping("/page")
    public R page(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer size) {
        List<Map<String, Object>> all = new ArrayList<>();
        List<SongComment> songComments = songCommentMapper.selectList(null);
        for (SongComment sc : songComments) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", sc.getId());
            m.put("type", "song");
            m.put("targetId", sc.getSongId());
            m.put("userId", sc.getUserId());
            m.put("content", sc.getContent());
            m.put("createTime", sc.getCreateTime());
            all.add(m);
        }
        List<PostComment> postComments = postCommentMapper.selectList(null);
        for (PostComment pc : postComments) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", pc.getId());
            m.put("type", "post");
            m.put("targetId", pc.getPostId());
            m.put("userId", pc.getUserId());
            m.put("content", pc.getContent());
            m.put("createTime", pc.getCreateTime());
            all.add(m);
        }
        all.sort((a, b) -> {
            java.util.Date da = (java.util.Date) a.get("createTime");
            java.util.Date db = (java.util.Date) b.get("createTime");
            if (da == null && db == null) return 0;
            if (da == null) return 1;
            if (db == null) return -1;
            return db.compareTo(da);
        });
        int total = all.size();
        int from = (page - 1) * size;
        int to = Math.min(from + size, total);
        List<Map<String, Object>> records = from < total ? all.subList(from, to) : new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        data.put("records", records);
        data.put("total", total);
        data.put("page", page);
        data.put("size", size);
        return R.success("查询成功", data);
    }

    @DeleteMapping("/delete")
    public R delete(@RequestParam Integer id, @RequestParam(required = false) String type) {
        if ("post".equals(type)) {
            postCommentMapper.deleteById(id);
        } else {
            songCommentMapper.deleteById(id);
        }
        return R.success("删除成功");
    }

    /**
     * 获取用户点赞过的评论列表
     * GET /comment/liked?userId=
     */
    @GetMapping("/liked")
    public R getUserLikedComments(@RequestParam Integer userId) {
        return commentLikeService.getUserLikedComments(userId);
    }
}

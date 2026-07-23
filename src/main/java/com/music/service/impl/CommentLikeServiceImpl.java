package com.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.music.common.R;
import com.music.mapper.CommentLikeMapper;
import com.music.mapper.PostCommentMapper;
import com.music.mapper.PostMapper;
import com.music.mapper.SongCommentMapper;
import com.music.mapper.SongMapper;
import com.music.mapper.UserMapper;
import com.music.model.domain.CommentLike;
import com.music.model.domain.Post;
import com.music.model.domain.PostComment;
import com.music.model.domain.Song;
import com.music.model.domain.SongComment;
import com.music.model.domain.User;
import com.music.model.request.CommentLikeRequest;
import com.music.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentLikeServiceImpl extends ServiceImpl<CommentLikeMapper, CommentLike> implements CommentLikeService {

    private final CommentLikeMapper commentLikeMapper;
    private final SongCommentMapper songCommentMapper;
    private final PostCommentMapper postCommentMapper;
    private final SongMapper songMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public R likeComment(CommentLikeRequest request) {
        Integer userId = request.getUserId();
        Integer commentId = request.getCommentId();
        String commentType = request.getCommentType();

        QueryWrapper<CommentLike> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("comment_id", commentId)
                .eq("comment_type", commentType);
        CommentLike record = getOne(wrapper);

        boolean isLike;
        if (record == null) {
            // 点赞
            CommentLike like = new CommentLike();
            like.setUserId(userId);
            like.setCommentId(commentId);
            like.setCommentType(commentType);
            like.setCreateTime(new Date());
            save(like);
            isLike = true;
        } else {
            // 取消点赞
            remove(wrapper);
            isLike = false;
        }

        // 更新对应评论表的 like_count
        int delta = isLike ? 1 : -1;
        if ("song".equals(commentType)) {
            SongComment comment = songCommentMapper.selectById(commentId);
            if (comment != null) {
                comment.setLikeCount(Math.max(0, comment.getLikeCount() + delta));
                songCommentMapper.updateById(comment);
            }
        } else if ("post".equals(commentType)) {
            PostComment comment = postCommentMapper.selectById(commentId);
            if (comment != null) {
                comment.setLikeCount(Math.max(0, comment.getLikeCount() + delta));
                postCommentMapper.updateById(comment);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("isLiked", isLike);
        return R.success(isLike ? "点赞成功" : "已取消点赞", result);
    }

    @Override
    public R getUserLikedComments(Integer userId) {
        QueryWrapper<CommentLike> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("create_time");
        List<CommentLike> likes = commentLikeMapper.selectList(wrapper);

        if (likes.isEmpty()) {
            return R.success("ok", Collections.emptyList());
        }

        // 分离歌曲评论和帖子评论
        List<Integer> songCommentIds = likes.stream()
                .filter(l -> "song".equals(l.getCommentType()))
                .map(CommentLike::getCommentId)
                .collect(Collectors.toList());
        List<Integer> postCommentIds = likes.stream()
                .filter(l -> "post".equals(l.getCommentType()))
                .map(CommentLike::getCommentId)
                .collect(Collectors.toList());

        // 查询评论内容
        Map<Integer, SongComment> songCommentMap = new HashMap<>();
        Map<Integer, PostComment> postCommentMap = new HashMap<>();
        Map<Integer, Song> songMap = new HashMap<>();
        Map<Integer, Post> postMap = new HashMap<>();
        Map<Integer, User> userMap = new HashMap<>();

        if (!songCommentIds.isEmpty()) {
            QueryWrapper<SongComment> scWrapper = new QueryWrapper<>();
            scWrapper.in("id", songCommentIds);
            List<SongComment> scList = songCommentMapper.selectList(scWrapper);
            songCommentMap = scList.stream().collect(Collectors.toMap(SongComment::getId, Function.identity()));

            // 查关联歌曲
            Set<Integer> songIds = scList.stream().map(SongComment::getSongId).filter(Objects::nonNull).collect(Collectors.toSet());
            if (!songIds.isEmpty()) {
                QueryWrapper<Song> songWrapper = new QueryWrapper<>();
                songWrapper.in("id", songIds);
                songMap = songMapper.selectList(songWrapper).stream()
                        .collect(Collectors.toMap(Song::getId, Function.identity()));
            }
        }

        if (!postCommentIds.isEmpty()) {
            QueryWrapper<PostComment> pcWrapper = new QueryWrapper<>();
            pcWrapper.in("id", postCommentIds);
            List<PostComment> pcList = postCommentMapper.selectList(pcWrapper);
            postCommentMap = pcList.stream().collect(Collectors.toMap(PostComment::getId, Function.identity()));

            // 查关联帖子
            Set<Integer> postIds = pcList.stream().map(PostComment::getPostId).filter(Objects::nonNull).collect(Collectors.toSet());
            if (!postIds.isEmpty()) {
                QueryWrapper<Post> postWrapper2 = new QueryWrapper<>();
                postWrapper2.in("id", postIds);
                postMap = postMapper.selectList(postWrapper2).stream()
                        .collect(Collectors.toMap(Post::getId, Function.identity()));
            }
        }

        // 收集所有评论作者 userId
        Set<Integer> allUserIds = new HashSet<>();
        songCommentMap.values().forEach(c -> { if (c.getUserId() != null) allUserIds.add(c.getUserId()); });
        postCommentMap.values().forEach(c -> { if (c.getUserId() != null) allUserIds.add(c.getUserId()); });
        if (!allUserIds.isEmpty()) {
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.in("id", allUserIds);
            userMap = userMapper.selectList(userWrapper).stream()
                    .collect(Collectors.toMap(User::getId, Function.identity()));
        }

        // 组装结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (CommentLike like : likes) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("likeTime", like.getCreateTime());
            item.put("commentType", like.getCommentType());

            if ("song".equals(like.getCommentType())) {
                SongComment sc = songCommentMap.get(like.getCommentId());
                if (sc == null) continue;
                item.put("commentId", sc.getId());
                item.put("content", sc.getContent());
                Song song = songMap.get(sc.getSongId());
                item.put("sourceName", song != null ? song.getName() : "");
                item.put("sourceId", sc.getSongId());
                item.put("sourceType", "song");
                User u = userMap.get(sc.getUserId());
                item.put("commentUsername", u != null ? u.getUsername() : "");
            } else {
                PostComment pc = postCommentMap.get(like.getCommentId());
                if (pc == null) continue;
                item.put("commentId", pc.getId());
                item.put("content", pc.getContent());
                Post post = postMap.get(pc.getPostId());
                item.put("sourceName", post != null ? post.getTitle() : "");
                item.put("sourceId", pc.getPostId());
                item.put("sourceType", "post");
                User u = userMap.get(pc.getUserId());
                item.put("commentUsername", u != null ? u.getUsername() : "");
            }
            result.add(item);
        }
        return R.success("ok", result);
    }
}

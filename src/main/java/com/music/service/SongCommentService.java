package com.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.music.common.R;
import com.music.model.domain.SongComment;
import com.music.model.request.CommentRequest;

public interface SongCommentService extends IService<SongComment> {
    // 新增评论/回复
    R addSongComment(CommentRequest request);
    // 删除自己的评论
    R deleteSongComment(Integer commentId, Integer loginUserId);
    // 查询歌曲全部评论（带用户昵称头像）
    R listSongComment(Integer songId);
}

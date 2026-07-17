package com.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.music.common.R;
import com.music.model.domain.Collect;
import com.music.model.request.CollectRequest;

public interface CollectService extends IService<Collect> {
    // 收藏/取消收藏
    R collectSong(CollectRequest request);
    // 查询用户所有收藏歌曲
    R getUserCollect(Integer userId);
    // 判断是否已收藏
    Boolean checkCollect(Integer userId, Integer songId);
}
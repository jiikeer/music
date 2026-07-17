package com.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.music.common.R;
import com.music.mapper.CollectMapper;
import com.music.model.domain.Collect;
import com.music.model.request.CollectRequest;
import com.music.service.CollectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {
    private final CollectMapper collectMapper;

    @Override
    @Transactional
    public R collectSong(CollectRequest request) {
        Integer userId = request.getUserId();
        Integer songId = request.getSongId();
        QueryWrapper<Collect> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("song_id", songId);
        Collect record = getOne(wrapper);
        if (record == null) {
            // 收藏
            Collect collect = new Collect();
            collect.setUserId(userId);
            collect.setSongId(songId);
            collect.setCreateTime(new Date());
            save(collect);
            return R.success("收藏成功");
        } else {
            // 取消收藏
            remove(wrapper);
            return R.success("取消收藏成功");
        }
    }

    @Override
    public R getUserCollect(Integer userId) {
        QueryWrapper<Collect> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("create_time");
        List<Collect> list = list(wrapper);
        return R.success("查询收藏列表成功", list);
    }

    @Override
    public Boolean checkCollect(Integer userId, Integer songId) {
        QueryWrapper<Collect> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("song_id", songId);
        return count(wrapper) > 0;
    }
}
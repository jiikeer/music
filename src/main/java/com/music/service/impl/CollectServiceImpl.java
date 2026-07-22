package com.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.music.common.R;
import com.music.mapper.CollectMapper;
import com.music.mapper.SongMapper;
import com.music.model.domain.Collect;
import com.music.model.domain.Song;
import com.music.model.request.CollectRequest;
import com.music.service.CollectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CollectServiceImpl extends ServiceImpl<CollectMapper, Collect> implements CollectService {
    private final CollectMapper collectMapper;
    private final SongMapper songMapper;

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

        // 拼装歌曲信息
        Set<Integer> songIds = list.stream().map(Collect::getSongId).collect(Collectors.toSet());
        Map<Integer, Song> songMap = new LinkedHashMap<>();
        if (!songIds.isEmpty()) {
            QueryWrapper<Song> songWrapper = new QueryWrapper<>();
            songWrapper.in("id", songIds);
            songMap = songMapper.selectList(songWrapper).stream()
                    .collect(Collectors.toMap(Song::getId, s -> s, (a, b) -> a, LinkedHashMap::new));
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Collect c : list) {
            Song s = songMap.get(c.getSongId());
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("songId", c.getSongId());
            map.put("userId", c.getUserId());
            map.put("createTime", c.getCreateTime());
            map.put("songName", s != null ? s.getName() : "");
            map.put("artist", s != null ? s.getIntroduction() : "");
            map.put("pic", s != null ? s.getPic() : "");
            map.put("url", s != null ? s.getUrl() : "");
            result.add(map);
        }
        return R.success("查询收藏列表成功", result);
    }

    @Override
    public Boolean checkCollect(Integer userId, Integer songId) {
        QueryWrapper<Collect> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("song_id", songId);
        return count(wrapper) > 0;
    }
}
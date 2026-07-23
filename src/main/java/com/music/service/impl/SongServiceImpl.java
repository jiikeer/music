package com.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.music.common.R;
import com.music.config.FileUploadConfig;
import com.music.mapper.SingerMapper;
import com.music.mapper.SongMapper;
import com.music.mapper.UserMapper;
import com.music.model.domain.Singer;
import com.music.model.domain.Song;
import com.music.model.domain.User;
import com.music.model.request.SongRequest;
import com.music.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class SongServiceImpl extends ServiceImpl<SongMapper,Song>
        implements SongService {

    private final SongMapper songMapper;

    private final UserMapper userMapper;

    private final SingerMapper singerMapper;

    private final FileUploadConfig uploadUtil;

    private static final String DEFAULT_AVATAR = "musicSystem/songPic/default.png";

    @Override
    public R uploadSong(SongRequest request,
                        MultipartFile songFile,
                        MultipartFile coverFile) {

        Song song = new Song();

        BeanUtils.copyProperties(request, song);

        try {

            String songUrl = uploadUtil.upload(songFile, "song");

            song.setUrl(songUrl);

            if (coverFile != null && !coverFile.isEmpty()) {

                song.setPic(uploadUtil.upload(coverFile, "songPic"));

            } else {

                song.setPic(DEFAULT_AVATAR);

            }

            // ====== 歌手字段强制绑定逻辑 ======
            // 普通用户上传：singer = 当前用户名, singerUserId = 当前用户ID
            // 管理员上传：接受前端传入的 singer 和 singerUserId
            Integer uploadUserId = request.getUserId();
            if (uploadUserId != null) {
                User uploadUser = userMapper.selectById(uploadUserId);
                if (uploadUser != null) {
                    // 管理员：保留前端传入的歌手名和 singerUserId
                    // 普通用户：强制绑定为自己
                    if (!"admin".equals(uploadUser.getUsername())) {
                        song.setSinger(uploadUser.getUsername());
                        song.setSingerUserId(uploadUserId);
                    } else {
                        // 管理员：前端传入 singer，自动匹配 singerUserId
                        if (song.getSinger() != null && !song.getSinger().isEmpty()) {
                            // 根据歌手名查用户表
                            QueryWrapper<User> userWrapper = new QueryWrapper<>();
                            userWrapper.eq("username", song.getSinger());
                            User matchedUser = userMapper.selectOne(userWrapper);
                            if (matchedUser != null) {
                                song.setSingerUserId(matchedUser.getId());
                            } else {
                                song.setSingerUserId(null);
                            }
                        }
                    }
                }
            }

            song.setStatus(0);

            song.setPlayCount(0);

            song.setCollectCount(0);

            song.setLikeCount(0);

            song.setCreateTime(new Date());

            song.setUpdateTime(new Date());

            songMapper.insert(song);

            return R.success("上传成功，等待管理员审核");

        } catch (Exception e) {

            return R.error(e.getMessage());

        }

    }

    @Override
    public R userSongs(Integer userId){

        QueryWrapper<Song> wrapper=new QueryWrapper<>();

        wrapper.eq("user_id",userId);

        wrapper.orderByDesc("create_time");
        List<Song> songList = songMapper.selectList(wrapper);
        return R.success("查询我的作品成功", songList);
    }

    @Override
    public R updateSong(SongRequest request, MultipartFile songFile, MultipartFile coverFile) {
        Integer songId = request.getId();
        if (songId == null) {
            return R.error("歌曲id不能为空");
        }
        Song oldSong = songMapper.selectById(songId);
        if (oldSong == null) {
            return R.error("不存在该歌曲");
        }

        BeanUtils.copyProperties(request, oldSong, "id", "userId");

        try {
            if (songFile != null && !songFile.isEmpty()) {
                String newSongUrl = uploadUtil.upload(songFile, "song");
                oldSong.setUrl(newSongUrl);
            }
            if (coverFile != null && !coverFile.isEmpty()) {
                String newCoverUrl = uploadUtil.upload(coverFile, "songPic");
                oldSong.setPic(newCoverUrl);
            }

            // 更新歌手字段时同步匹配 singerUserId
            if (oldSong.getSinger() != null && !oldSong.getSinger().isEmpty()) {
                QueryWrapper<User> userWrapper = new QueryWrapper<>();
                userWrapper.eq("username", oldSong.getSinger());
                User matchedUser = userMapper.selectOne(userWrapper);
                oldSong.setSingerUserId(matchedUser != null ? matchedUser.getId() : null);
            }

            oldSong.setUpdateTime(new Date());
            int rows = songMapper.updateById(oldSong);
            if (rows > 0) {
                return R.success("歌曲修改成功");
            } else {
                return R.error("未修改任何数据");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return R.error("修改失败：" + e.getMessage());
        }
    }

    @Override
    public R songDetail(Integer id) {
        Song song = songMapper.selectById(id);
        if (song != null && song.getSingerId() != null) {
            Singer singer = singerMapper.selectById(song.getSingerId());
            if (singer != null) {
                song.setSingerName(singer.getName());
            }
        }
        // 优先从 singerUserId 关联查最新用户名
        if (song != null && song.getSingerUserId() != null) {
            User singerUser = userMapper.selectById(song.getSingerUserId());
            if (singerUser != null) {
                song.setSingerName(singerUser.getUsername());
                if (song.getSinger() == null) {
                    song.setSinger(singerUser.getUsername());
                }
            }
        }
        return R.success("查询歌曲详情成功", song);
    }

    @Override
    public R deleteSong(Integer id){

        if(songMapper.deleteById(id)>0){

            return R.success("删除成功");

        }

        return R.error("删除失败");

    }

    @Override
    public R auditSong(Integer id, Integer status, String auditReason) {
        // 校验歌曲是否存在
        Song song = songMapper.selectById(id);
        if (song == null) {
            return R.error("不存在该歌曲");
        }
        // 校验状态只能是1/2
        if (!status.equals(1) && !status.equals(2)) {
            return R.error("审核状态仅支持：1通过 / 2驳回");
        }
        // 封装更新数据
        Song updateSong = new Song();
        updateSong.setId(id);
        updateSong.setStatus(status);
        updateSong.setAuditReason(auditReason);
        updateSong.setUpdateTime(new Date());
        // 执行更新
        int rows = songMapper.updateById(updateSong);
        if (rows > 0) {
            return R.success("歌曲审核操作完成");
        } else {
            return R.error("审核失败，数据无变更");
        }
    }

    @Override
    public R adminPageSong(Integer page, Integer size, Integer status) {
        Page<Song> pageInfo = new Page<>(page, size);
        QueryWrapper<Song> wrapper = new QueryWrapper<>();
        if(status != null){
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        songMapper.selectPage(pageInfo, wrapper);

        // 批量填充 username
        List<Song> records = pageInfo.getRecords();
        Set<Integer> userIds = records.stream()
                .map(Song::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toCollection(HashSet::new));
        if (!userIds.isEmpty()) {
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.in("id", userIds);
            Map<Integer, User> userMap = userMapper.selectList(userWrapper).stream()
                    .collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));
            records.forEach(s -> {
                User u = userMap.get(s.getUserId());
                if (u != null) s.setUsername(u.getUsername());
            });
        }

        return R.success("查询成功", pageInfo);
    }

    @Override
    public R hotSongs(Integer limit) {
        if (limit == null || limit <= 0) limit = 5;
        QueryWrapper<Song> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.orderByDesc("play_count");
        wrapper.last("LIMIT " + limit);
        List<Song> songs = songMapper.selectList(wrapper);
        return R.success("查询热门歌曲成功", songs);
    }

    @Override
    public R songsBySingerUserId(Integer singerUserId, Integer page, Integer size) {
        Page<Song> pageInfo = new Page<>(page != null ? page : 1, size != null ? size : 20);
        QueryWrapper<Song> wrapper = new QueryWrapper<>();
        wrapper.eq("singer_user_id", singerUserId)
               .eq("status", 1)
               .orderByDesc("create_time");
        songMapper.selectPage(pageInfo, wrapper);
        return R.success("查询成功", pageInfo);
    }

    @Override
    public R incrementPlayCount(Integer id) {
        Song song = songMapper.selectById(id);
        if (song == null) return R.error("歌曲不存在");
        song.setPlayCount((song.getPlayCount() == null ? 0 : song.getPlayCount()) + 1);
        songMapper.updateById(song);
        return R.success("ok");
    }

}
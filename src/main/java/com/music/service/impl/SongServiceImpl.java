package com.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.music.common.R;
import com.music.config.FileUploadConfig;
import com.music.mapper.SongMapper;
import com.music.model.domain.Song;
import com.music.model.request.SongRequest;
import com.music.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SongServiceImpl extends ServiceImpl<SongMapper,Song>
        implements SongService {

    private final SongMapper songMapper;
    private final FileUploadConfig uploadUtil;
    private static final String DEFAULT_AVATAR = "songPic/default.png";

    /**
     * 读取txt文件内容，转为歌词字符串
     */
    /**
     * 读取txt文件内容，转为歌词字符串（兼容JDK8）
     */
    private String readLyricFile(MultipartFile lyricFile) throws Exception{
        if(lyricFile == null || lyricFile.isEmpty()){
            return null;
        }
        // 简单校验后缀，只允许txt
        String fileName = lyricFile.getOriginalFilename();
        if(fileName == null || !fileName.endsWith(".txt")){
            throw new RuntimeException("歌词仅支持txt格式文件");
        }
        try (InputStream inputStream = lyricFile.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            // 移除最后多余换行（可选）
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }
            return sb.toString();
        }
    }
    @Override
    public R uploadSong(SongRequest request,
                        MultipartFile songFile,
                        MultipartFile coverFile,
                        MultipartFile lyricFile) {

        Song song = new Song();
        BeanUtils.copyProperties(request, song);

        try {
            // 如果上传了歌词txt，读取内容覆盖lyric
            String lyricContent = readLyricFile(lyricFile);
            if(lyricContent != null){
                song.setLyric(lyricContent);
            }

            String songUrl = uploadUtil.upload(songFile, "song");
            song.setUrl(songUrl);

            if (coverFile != null && !coverFile.isEmpty()) {
                song.setPic(uploadUtil.upload(coverFile, "songPic"));
            } else {
                song.setPic(DEFAULT_AVATAR);
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
            e.printStackTrace();
            return R.error(e.getMessage());
        }
    }

    @Override
    public R updateSong(SongRequest request,
                        MultipartFile songFile,
                        MultipartFile coverFile,
                        MultipartFile lyricFile) {
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
            // 有歌词文件则读取覆盖
            String lyricContent = readLyricFile(lyricFile);
            if(lyricContent != null){
                oldSong.setLyric(lyricContent);
            }

            if (songFile != null && !songFile.isEmpty()) {
                String newSongUrl = uploadUtil.upload(songFile, "song");
                oldSong.setUrl(newSongUrl);
            }
            if (coverFile != null && !coverFile.isEmpty()) {
                String newCoverUrl = uploadUtil.upload(coverFile, "songPic");
                oldSong.setPic(newCoverUrl);
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
    public R userSongs(Integer userId){
        QueryWrapper<Song> wrapper=new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.orderByDesc("create_time");
        List<Song> songList = songMapper.selectList(wrapper);
        return R.success("查询我的作品成功", songList);
    }

    @Override
    public R songDetail(Integer id) {
        Song song = songMapper.selectById(id);
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
        Song song = songMapper.selectById(id);
        if (song == null) {
            return R.error("不存在该歌曲");
        }
        if (!status.equals(1) && !status.equals(2)) {
            return R.error("审核状态仅支持：1通过 / 2驳回");
        }
        Song updateSong = new Song();
        updateSong.setId(id);
        updateSong.setStatus(status);
        updateSong.setAuditReason(auditReason);
        updateSong.setUpdateTime(new Date());
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
        return R.success("查询成功", pageInfo);
    }

    // SongServiceImpl.java
    @Override
    public R getHotSongList(Integer limit) {
        if(limit == null || limit <= 0){
            limit = 5;
        }
        QueryWrapper<Song> wrapper = new QueryWrapper<>();
        // 只展示审核通过歌曲
        wrapper.eq("status", 1);
        // 播放量降序
        wrapper.orderByDesc("play_count");
        // 限制条数
        wrapper.last("limit " + limit);
        List<Song> hotList = songMapper.selectList(wrapper);
        return R.success("查询热门歌曲成功", hotList);
    }

}
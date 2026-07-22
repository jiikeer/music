package com.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.music.common.R;
import com.music.model.domain.Song;
import com.music.model.request.SongRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SongService extends IService<Song> {

    // 新增 lyricFile 参数
    R uploadSong(SongRequest request,
                 MultipartFile songFile,
                 MultipartFile coverFile,
                 MultipartFile lyricFile);

    // 新增 lyricFile 参数
    R updateSong(SongRequest request,
                 MultipartFile songFile,
                 MultipartFile coverFile,
                 MultipartFile lyricFile);

    R deleteSong(Integer id);

    R songDetail(Integer id);

    R userSongs(Integer userId);

    R adminPageSong(Integer page,Integer size,Integer status);

    R auditSong(Integer id,Integer status,String auditReason);

    R getHotSongList(Integer limit);
}
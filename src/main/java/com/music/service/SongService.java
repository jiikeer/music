package com.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.music.common.R;
import com.music.model.domain.Song;
import com.music.model.request.SongRequest;
import org.springframework.web.multipart.MultipartFile;

public interface SongService extends IService<Song> {

    R uploadSong(SongRequest request,
                 MultipartFile songFile,
                 MultipartFile coverFile);

    R updateSong(SongRequest request, MultipartFile songFile, MultipartFile coverFile);

    R deleteSong(Integer id);

    R songDetail(Integer id);

    R userSongs(Integer userId);

    R adminPageSong(Integer page,Integer size,Integer status);

    R auditSong(Integer id,Integer status,String auditReason);

}
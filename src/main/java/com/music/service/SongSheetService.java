package com.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.music.common.R;
import com.music.model.domain.SongSheet;
import org.springframework.web.multipart.MultipartFile;

public interface SongSheetService extends IService<SongSheet> {
    R allSongSheet();
    R pageSongSheet(Integer page, Integer size);
    R songSheetDetail(Integer id);
    R addSongSheet(SongSheet songSheet);
    R updateSongSheet(SongSheet songSheet);
    R deleteSongSheet(Integer id);
    R updateSongSheetPic(MultipartFile file, Integer id);
}

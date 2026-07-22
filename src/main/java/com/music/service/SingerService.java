package com.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.music.common.R;
import com.music.model.domain.Singer;
import org.springframework.web.multipart.MultipartFile;

public interface SingerService extends IService<Singer> {
    R allSinger();
    R pageSinger(Integer page, Integer size);
    R singerDetail(Integer id);
    R addSinger(Singer singer);
    R updateSinger(Singer singer);
    R deleteSinger(Integer id);
    R updateSingerAvatar(MultipartFile file, Integer id);
}

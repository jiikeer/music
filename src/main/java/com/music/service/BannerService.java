package com.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.music.common.R;
import com.music.model.domain.Banner;
import org.springframework.web.multipart.MultipartFile;

public interface BannerService extends IService<Banner> {
    R allBanner();
    R pageBanner(Integer page, Integer size);
    R addBanner(Banner banner);
    R updateBanner(Banner banner);
    R deleteBanner(Integer id);
    R updateBannerPic(MultipartFile file, Integer id);
}

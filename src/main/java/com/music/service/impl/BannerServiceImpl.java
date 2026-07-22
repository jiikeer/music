package com.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.music.common.R;
import com.music.config.FileUploadConfig;
import com.music.mapper.BannerMapper;
import com.music.model.domain.Banner;
import com.music.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    private final BannerMapper bannerMapper;
    private final FileUploadConfig uploadUtil;

    @Override
    public R allBanner() {
        return R.success(null, bannerMapper.selectList(null));
    }

    @Override
    public R pageBanner(Integer page, Integer size) {
        Page<Banner> pageInfo = new Page<>(page, size);
        bannerMapper.selectPage(pageInfo, new QueryWrapper<Banner>().orderByAsc("sort"));
        return R.success("查询成功", pageInfo);
    }

    @Override
    public R addBanner(Banner banner) {
        banner.setCreateTime(new Date());
        bannerMapper.insert(banner);
        return R.success("添加成功");
    }

    @Override
    public R updateBanner(Banner banner) {
        banner.setUpdateTime(new Date());
        bannerMapper.updateById(banner);
        return R.success("更新成功");
    }

    @Override
    public R deleteBanner(Integer id) {
        bannerMapper.deleteById(id);
        return R.success("删除成功");
    }

    @Override
    public R updateBannerPic(MultipartFile file, Integer id) {
        try {
            String picUrl = uploadUtil.upload(file, "banner");
            Banner banner = new Banner();
            banner.setId(id);
            banner.setPic(picUrl);
            bannerMapper.updateById(banner);
            return R.success("上传成功");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }
}

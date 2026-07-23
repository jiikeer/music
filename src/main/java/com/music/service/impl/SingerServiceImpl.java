package com.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.music.common.R;
import com.music.config.FileUploadConfig;
import com.music.mapper.SingerMapper;
import com.music.model.domain.Singer;
import com.music.service.SingerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class SingerServiceImpl extends ServiceImpl<SingerMapper, Singer> implements SingerService {

    private final SingerMapper singerMapper;
    private final FileUploadConfig uploadUtil;

    @Override
    public R allSinger() {
        return R.success(null, singerMapper.selectList(null));
    }

    @Override
    public R pageSinger(Integer page, Integer size) {
        Page<Singer> pageInfo = new Page<>(page, size);
        singerMapper.selectPage(pageInfo, new QueryWrapper<Singer>().orderByDesc("create_time"));
        return R.success("查询成功", pageInfo);
    }

    @Override
    public R singerDetail(Integer id) {
        return R.success(null, singerMapper.selectById(id));
    }

    @Override
    public R addSinger(Singer singer) {
        singer.setCreateTime(new Date());
        singerMapper.insert(singer);
        return R.success("添加成功", singer.getId());
    }

    @Override
    public R updateSinger(Singer singer) {
        singer.setUpdateTime(new Date());
        singerMapper.updateById(singer);
        return R.success("更新成功", singer.getId());
    }

    @Override
    public R deleteSinger(Integer id) {
        singerMapper.deleteById(id);
        return R.success("删除成功");
    }

    @Override
    public R updateSingerAvatar(MultipartFile file, Integer id) {
        try {
            String picUrl = uploadUtil.upload(file, "singerPic");
            Singer singer = new Singer();
            singer.setId(id);
            singer.setPic(picUrl);
            singerMapper.updateById(singer);
            return R.success("上传成功", picUrl);
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }
}

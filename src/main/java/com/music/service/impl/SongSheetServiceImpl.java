package com.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.music.common.R;
import com.music.config.FileUploadConfig;
import com.music.mapper.SongSheetMapper;
import com.music.model.domain.SongSheet;
import com.music.service.SongSheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class SongSheetServiceImpl extends ServiceImpl<SongSheetMapper, SongSheet> implements SongSheetService {

    private final SongSheetMapper songSheetMapper;
    private final FileUploadConfig uploadUtil;

    @Override
    public R allSongSheet() {
        return R.success(null, songSheetMapper.selectList(null));
    }

    @Override
    public R pageSongSheet(Integer page, Integer size) {
        Page<SongSheet> pageInfo = new Page<>(page, size);
        songSheetMapper.selectPage(pageInfo, new QueryWrapper<SongSheet>().orderByDesc("create_time"));
        return R.success("查询成功", pageInfo);
    }

    @Override
    public R songSheetDetail(Integer id) {
        return R.success(null, songSheetMapper.selectById(id));
    }

    @Override
    public R addSongSheet(SongSheet songSheet) {
        songSheet.setCreateTime(new Date());
        songSheetMapper.insert(songSheet);
        return R.success("添加成功");
    }

    @Override
    public R updateSongSheet(SongSheet songSheet) {
        songSheet.setUpdateTime(new Date());
        songSheetMapper.updateById(songSheet);
        return R.success("更新成功");
    }

    @Override
    public R deleteSongSheet(Integer id) {
        songSheetMapper.deleteById(id);
        return R.success("删除成功");
    }

    @Override
    public R updateSongSheetPic(MultipartFile file, Integer id) {
        try {
            String picUrl = uploadUtil.upload(file, "songSheetPic");
            SongSheet songSheet = new SongSheet();
            songSheet.setId(id);
            songSheet.setPic(picUrl);
            songSheetMapper.updateById(songSheet);
            return R.success("上传成功");
        } catch (Exception e) {
            return R.error(e.getMessage());
        }
    }
}

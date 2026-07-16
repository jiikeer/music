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
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SongServiceImpl extends ServiceImpl<SongMapper,Song>
        implements SongService {

    private final SongMapper songMapper;

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
    public R pageSong(Integer page,Integer size){

        Page<Song> pageInfo=new Page<>(page,size);

        QueryWrapper<Song> wrapper=new QueryWrapper<>();

        wrapper.eq("status",1);

        wrapper.orderByDesc("create_time");

        songMapper.selectPage(pageInfo,wrapper);

        return R.success("查询成功",pageInfo);

    }

    @Override
    public R updateSong(SongRequest request){

        Song song=new Song();

        BeanUtils.copyProperties(request,song);

        song.setUpdateTime(new Date());

        if(songMapper.updateById(song)>0){

            return R.success("修改成功");

        }

        return R.error("修改失败");

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
    public R auditSong(Integer id,Integer status,String reason){

        Song song=new Song();

        song.setId(id);

        song.setStatus(status);

        song.setAuditReason(reason);

        song.setUpdateTime(new Date());

        songMapper.updateById(song);

        return R.success("审核完成");

    }
}
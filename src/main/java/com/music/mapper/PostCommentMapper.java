package com.music.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.model.domain.PostComment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PostCommentMapper extends BaseMapper<PostComment> {
}
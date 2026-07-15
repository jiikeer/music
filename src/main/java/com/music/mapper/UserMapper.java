package com.music.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.music.model.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}

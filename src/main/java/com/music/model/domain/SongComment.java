package com.music.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("song_comment")
public class SongComment {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer songId;
    private Integer userId;
    private String content;
    private Date createTime;
    private Integer parentId;
    private Integer likeCount;
}
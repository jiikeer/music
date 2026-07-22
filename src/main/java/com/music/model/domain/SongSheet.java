package com.music.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@TableName("song_sheet")
@Data
public class SongSheet {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private String pic;
    private String introduction;
    private Integer style;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}

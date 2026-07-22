package com.music.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@TableName("banner")
@Data
public class Banner {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String pic;
    private String title;
    private String url;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}

package com.music.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.Date;

@TableName("singer")
@Data
public class Singer {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String sex;
    private String pic;
    private Date birth;
    private String location;
    private String introduction;
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}

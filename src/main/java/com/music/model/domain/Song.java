package com.music.model.domain;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

@TableName(value = "song")
@Data
public class Song {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String name;

    private String introduction;

    private Integer singerId;

    /** Singer name (redundant, fallback display) */
    private String singer;

    /** Singer user ID, references user.id; null for external singers */
    private Integer singerUserId;

    private Integer status;

    private String auditReason;

    private Integer playCount;

    private Integer collectCount;

    private Integer likeCount;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    private String pic;

    private String lyric;

    private String url;

    /**
     * Song duration in seconds.
     */
    private Integer duration;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String singerName;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}

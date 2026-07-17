package com.music.model.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("post_comment")
public class PostComment {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer postId;
    private Integer userId;
    private String content;
    private Date createTime;
    private Integer parentId;
    private Integer likeCount;
}
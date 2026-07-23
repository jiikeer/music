package com.music.model.request;

import lombok.Data;

@Data
public class SongRequest {

    private Integer id;

    private Integer userId;

    private String name;

    private String introduction;

    /** Singer name (admin editable, ignored for normal users) */
    private String singer;

    /** Singer user ID (admin editable, ignored for normal users) */
    private Integer singerUserId;

    private String lyric;
}

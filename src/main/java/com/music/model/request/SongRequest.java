package com.music.model.request;

import lombok.Data;

@Data
public class SongRequest {

    private Integer id;

    private Integer userId;

    private String name;

    private String introduction;

    private String lyric;

    private String pic;

    private String url;

    private Integer duration;
}

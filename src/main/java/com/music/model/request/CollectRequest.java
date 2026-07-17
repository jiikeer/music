package com.music.model.request;
import lombok.Data;

@Data
public class CollectRequest {
    private Integer userId;
    private Integer songId;
}
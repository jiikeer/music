package com.music.model.request;

import lombok.Data;

import java.util.Date;

@Data
public class UserRequest {
    private Integer id;

    private String username;

    private String oldPassword;

    private String password;

    private Byte sex;

    private String phoneNum;

    private String email;

    private Date birth;

    private String introduction;

    private String avatar;

}

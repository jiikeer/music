package com.music.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

@Data
public class UserRequest {

    private Integer id;
    private String username;
    private String oldPassword;
    private String password;
    private String sex;
    private String phoneNum;
    private String email;
    @JsonFormat(
            pattern = "yyyy-MM-dd",
            timezone = "GMT+8"
    )
    private Date birth;
    private String introduction;
    private String avatar;

}
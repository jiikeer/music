package com.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.music.common.R;
import com.music.model.domain.User;
import com.music.model.request.UserRequest;
import javax.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends IService<User> {

    R addUser(UserRequest registryRequest);

    R updateUserMsg(UserRequest updateRequest);

    R updateUserAvatar(MultipartFile avatarFile, int id);

    R updatePassword(UserRequest updatePasswordRequest);

    boolean existUser(String username);

    boolean verityPasswd(String username, String password);

    R deleteUser(Integer id);

    R allUser();

    R pageUser(Integer page, Integer size);

    R userOfId(Integer id);

    R loginStatus(UserRequest loginRequest, HttpSession session);

    User findAppUserByLoginAccount(String account);
}

package com.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.music.common.R;
import com.music.config.FileConfig;
import com.music.config.FileUploadConfig;
import com.music.mapper.UserMapper;
import com.music.model.domain.User;
import com.music.model.request.UserRequest;
import com.music.service.UserService;
import javax.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.music.common.Constants.SALT;

@RequiredArgsConstructor
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private static final String DEFAULT_AVATAR = "avatar/user.jpg";


    private final UserMapper appUserMapper;

    private final PasswordEncoder passwordEncoder;
    private final FileConfig fileConfig;
    private final FileUploadConfig fileUploadConfig;

    @Override
    public R addUser(UserRequest registryRequest) {
        if (this.existUser(registryRequest.getUsername())) {
            return R.warning("用户名已注册");
        }
        User appUser = new User();
        BeanUtils.copyProperties(registryRequest, appUser);
        String password = passwordEncoder.encode(registryRequest.getPassword());
        appUser.setPassword(password);
        if (StringUtils.isBlank(appUser.getPhoneNum())) {
            appUser.setPhoneNum(null);
        }
        if ("".equals(appUser.getEmail())) {
            appUser.setEmail(null);
        }
        if (StringUtils.isBlank(appUser.getAvatar())) {
            appUser.setAvatar(DEFAULT_AVATAR);
        } else if (!appUser.getAvatar().startsWith("/")) {
            appUser.setAvatar("/" + appUser.getAvatar());
        }
        try {
            if (appUserMapper.insert(appUser) > 0) {
                return R.success("注册成功");
            } else {
                return R.error("注册失败");
            }
        } catch (DuplicateKeyException e) {
            return R.fatal(e.getMessage());
        }
    }

    @Override
    public R updateUserMsg(UserRequest updateRequest) {
        User appUser = new User();
        BeanUtils.copyProperties(updateRequest, appUser);
        if (appUserMapper.updateById(appUser) > 0) {
            return R.success("修改成功");
        } else {
            return R.error("修改失败");
        }
    }

    @Override
    public R updatePassword(UserRequest updatePasswordRequest) {

        if (!this.verityPasswd(updatePasswordRequest.getUsername(), updatePasswordRequest.getOldPassword())) {
            return R.error("密码输入错误");
        }

        User appUser = new User();
        appUser.setId(updatePasswordRequest.getId());
        appUser.setPassword(passwordEncoder.encode(updatePasswordRequest.getPassword()));

        if (appUserMapper.updateById(appUser) > 0) {
            return R.success("密码修改成功");
        } else {
            return R.error("密码修改失败");
        }
    }

    @Override
    public R updateUserAvatar(MultipartFile avatarFile, int id) {
        try {
            String imgPath=fileUploadConfig.upload(avatarFile,"avatar");
            User appUser = new User();
            appUser.setId(id);
            appUser.setAvatar(imgPath);
            if (appUserMapper.updateById(appUser) > 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("url", imgPath);
                return R.success("上传成功", data);
            } else {
                return R.error("更新头像失败");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return R.fatal("图片上传失败：" + e.getMessage());
        }
    }

    @Override
    public boolean existUser(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return appUserMapper.selectCount(queryWrapper) > 0;
    }

    public User findAppUserByLoginAccount(String account) {
        if (StringUtils.isBlank(account)) {
            return null;
        }
        String trimmed = account.trim();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(w -> w.eq("username", trimmed).or().eq("email", trimmed));
        return appUserMapper.selectOne(queryWrapper);
    }

    private boolean matchesStoredPassword(User appUser, String password) {
        if (appUser == null || StringUtils.isBlank(appUser.getPassword())) {
            return false;
        }
        String storedPassword = appUser.getPassword();
        if (passwordEncoder.matches(password, storedPassword)) {
            return true;
        }

        String legacyPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes(StandardCharsets.UTF_8));
        if (legacyPassword.equals(storedPassword)) {
            User update = new User();
            update.setId(appUser.getId());
            update.setPassword(passwordEncoder.encode(password));
            appUserMapper.updateById(update);
            return true;
        }
        return false;
    }

    @Override
    public boolean verityPasswd(String username, String password) {
        User appUser = findAppUserByLoginAccount(username);
        return matchesStoredPassword(appUser, password);
    }

    @Override
    public R deleteUser(Integer id) {
        if (appUserMapper.deleteById(id) > 0) {
            return R.success("删除成功");
        } else {
            return R.error("删除失败");
        }
    }

    @Override
    public R allUser() {
        return R.success(null, appUserMapper.selectList(null));
    }

    @Override
    public R pageUser(Integer page, Integer size) {
        Page<User> pageInfo = new Page<>(page, size);
        appUserMapper.selectPage(pageInfo, null);
        Map<String, Object> data = new HashMap<>();
        data.put("records", pageInfo.getRecords());
        data.put("total", pageInfo.getTotal());
        data.put("page", page);
        data.put("size", size);
        data.put("hasMore", page * size < pageInfo.getTotal());
        return R.success(null, data);
    }

    @Override
    public R userOfId(Integer id) {
        User appUser = appUserMapper.selectById(id);
        return R.success(null, appUser);
    }

    @Override
    public R loginStatus(UserRequest loginRequest, HttpSession session) {

        String account = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        if (!this.verityPasswd(account, password)) {
            return R.error("用户名或密码错误");
        }
        User appUser = findAppUserByLoginAccount(account);
        session.setAttribute("username", appUser.getUsername());
        return R.success("登录成功", appUser);
    }
}

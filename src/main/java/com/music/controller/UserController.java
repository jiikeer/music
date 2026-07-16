package com.music.controller;

import com.music.common.R;
import com.music.model.request.UserRequest;
import com.music.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService appUserService;

    @PostMapping("/user/add")
    public R addUser(@RequestBody UserRequest registryRequest) {
        return appUserService.addUser(registryRequest);
    }



    @GetMapping("/user")
    public R allUser() {
        return appUserService.allUser();
    }

    @GetMapping("/user/page")
    public R pageUser(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        return appUserService.pageUser(safePage, safeSize);
    }

    @GetMapping("/user/detail")
    public R userOfId(@RequestParam int id) {
        return appUserService.userOfId(id);
    }

    @GetMapping("/user/delete")
    public R deleteUser(@RequestParam int id) {
        return appUserService.deleteUser(id);
    }

    @PostMapping("/user/update")
    public R updateUserMsg(@RequestBody UserRequest updateRequest) {
        return appUserService.updateUserMsg(updateRequest);
    }

    @PostMapping("/user/updatePassword")
    public R updatePassword(@RequestBody UserRequest updatePasswordRequest) {
        return appUserService.updatePassword(updatePasswordRequest);
    }

    @PostMapping("/user/avatar/update")
    public R updateUserPic(@RequestParam("file") MultipartFile avatarFile, @RequestParam("id") int id) {
        return appUserService.updateUserAvatar(avatarFile, id);
    }
}

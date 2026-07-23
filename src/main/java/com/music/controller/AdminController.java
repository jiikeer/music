package com.music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.music.common.R;
import com.music.model.domain.Post;
import com.music.model.domain.Song;
import com.music.model.domain.User;
import com.music.service.PostService;
import com.music.service.SongService;
import com.music.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SongService songService;
    private final PostService postService;
    private final UserService appUserService;

    @PostMapping("/login")
    public R login(@RequestBody Map<String, String> data, javax.servlet.http.HttpSession session) {
        String username = data.get("username");
        String password = data.get("password");
        if (!appUserService.verityPasswd(username, password)) {
            return R.error("用户名或密码错误");
        }
        User user = appUserService.findAppUserByLoginAccount(username);
        session.setAttribute("username", user.getUsername());
        Map<String, Object> result = new HashMap<>();
        result.put("username", user.getUsername());
        result.put("roles", Arrays.asList("admin"));
        result.put("avatar", user.getAvatar());
        return R.success("登录成功", result);
    }

    @GetMapping("/info")
    public R info(javax.servlet.http.HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return R.error("未登录");
        }
        User user = appUserService.findAppUserByLoginAccount(username);
        Map<String, Object> result = new HashMap<>();
        result.put("username", user.getUsername());
        result.put("roles", Arrays.asList("admin"));
        result.put("avatar", user.getAvatar());
        return R.success("ok", result);
    }

    // ====================== 仪表盘统计 ======================
    @GetMapping("/dashboard")
    public R dashboard() {
        Map<String, Object> data = new HashMap<>();
        // 歌曲统计
        QueryWrapper<Song> songWrapper = new QueryWrapper<>();
        songWrapper.eq("status", 1);
        data.put("songCount", songService.count(songWrapper));
        QueryWrapper<Song> pendingSongWrapper = new QueryWrapper<>();
        pendingSongWrapper.eq("status", 0);
        data.put("pendingSongCount", songService.count(pendingSongWrapper));

        // 帖子统计
        QueryWrapper<Post> postWrapper = new QueryWrapper<>();
        postWrapper.eq("status", 1);
        data.put("postCount", postService.count(postWrapper));
        QueryWrapper<Post> pendingPostWrapper = new QueryWrapper<>();
        pendingPostWrapper.eq("status", 0);
        data.put("pendingPostCount", postService.count(pendingPostWrapper));

        // 用户统计
        data.put("userCount", appUserService.count());

        return R.success("ok", data);
    }

    // ====================== 歌曲管理模块 /admin/song ======================
    @GetMapping("/song/page")
    public R songPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status
    ) {
        return songService.adminPageSong(page, size, status);
    }

    @PostMapping("/song/audit")
    public R auditSong(
            @RequestParam Integer id,
            @RequestParam Integer status,
            @RequestParam(required = false) String auditReason
    ) {
        return songService.auditSong(id, status, auditReason);
    }

    @GetMapping("/song/delete")
    public R deleteSong(@RequestParam Integer id) {
        return songService.deleteSong(id);
    }

    @GetMapping("/song/detail")
    public R songDetail(@RequestParam Integer id) {
        return songService.songDetail(id);
    }

    // ====================== 帖子管理模块 /admin/post ======================
    @GetMapping("/post/page")
    public R postPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status
    ) {
        return postService.adminPagePost(page, size, status);
    }

    @PostMapping("/post/audit")
    public R auditPost(
            @RequestParam Integer postId,
            @RequestParam Integer status,
            @RequestParam(required = false) String auditReason
    ) {
        return postService.auditPost(postId, status, auditReason);
    }

    @DeleteMapping("/post/delete")
    public R deletePost(@RequestParam Integer postId) {
        return postService.adminDeletePost(postId);
    }

    @GetMapping("/post/detail")
    public R postDetail(@RequestParam Integer postId) {
        return postService.postDetail(postId);
    }
}

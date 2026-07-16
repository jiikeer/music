package com.music.controller;

import com.music.common.R;
import com.music.service.PostService;
import com.music.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SongService songService;
    private final PostService postService;

    // ====================== 歌曲管理模块 /admin/song ======================
    /**
     * 分页查询全站歌曲（管理员，可筛选审核状态 0待审核/1通过/2驳回）
     */
    @GetMapping("/song/page")
    public R songPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status
    ) {
        return songService.adminPageSong(page, size, status);
    }

    /**
     * 歌曲审核：通过 / 驳回，填写驳回理由
     */
    @PostMapping("/song/audit")
    public R auditSong(
            @RequestParam Integer id,
            @RequestParam Integer status,
            @RequestParam(required = false) String auditReason
    ) {
        return songService.auditSong(id, status, auditReason);
    }

    /**
     * 管理员删除任意歌曲（不受发布人限制）
     */
    @DeleteMapping("/song/delete")
    public R deleteSong(@RequestParam Integer id) {
        return songService.deleteSong(id);
    }


    /**
     * 歌曲详情
     */
    @GetMapping("/song/detail")
    public R songDetail(@RequestParam Integer id) {
        return songService.songDetail(id);
    }

    // ====================== 帖子管理模块 /admin/post ======================
    /**
     * 分页查询全站帖子（管理员，可筛选审核状态 0待审核/1通过/2驳回）
     */
    @GetMapping("/post/page")
    public R postPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status
    ) {
        return postService.adminPagePost(page, size, status);
    }

    /**
     * 帖子审核：通过 / 驳回，填写驳回理由
     */
    @PostMapping("/post/audit")
    public R auditPost(
            @RequestParam Integer postId,
            @RequestParam Integer status,
            @RequestParam(required = false) String auditReason
    ) {
        return postService.auditPost(postId, status, auditReason);
    }

    /**
     * 管理员删除任意帖子（不受发布人限制）
     */
    @DeleteMapping("/post/delete")
    public R deletePost(@RequestParam Integer postId) {
        return postService.adminDeletePost(postId);
    }

    /**
     * 帖子详情
     */
    @GetMapping("/post/detail")
    public R postDetail(@RequestParam Integer postId) {
        return postService.postDetail(postId);
    }
}

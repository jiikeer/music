package com.music.controller;

import com.music.common.R;
import com.music.model.request.PostRequest;
import com.music.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    /**
     * 接口1：发布帖子
     * 请求方式：POST
     * 请求类型：form-data
     * 参数：userId、title、content、coverFile(可选图片)
     * 功能：上传封面+新增帖子，默认待审核
     */
    @PostMapping("/publish")
    public R publish(
            PostRequest request,
            @RequestParam(value = "coverFile", required = false) MultipartFile coverFile
    ) {
        request.setCoverFile(coverFile);
        return postService.publishPost(request);
    }

    /**
     * 接口2：修改帖子
     * 请求方式：POST
     * 请求类型：form-data
     * 参数：id、title、content、coverFile(可选新封面)
     * 功能：修改标题/正文，可替换封面图片
     */
    @PostMapping("/update")
    public R update(
            PostRequest request,
            @RequestParam(value = "coverFile", required = false) MultipartFile coverFile
    ) {
        request.setCoverFile(coverFile);
        return postService.updatePost(request);
    }

    /**
     * 接口3：删除帖子
     * 请求方式：DELETE
     * 参数：postId、loginUserId 当前登录用户id
     * 功能：校验作者权限后删除帖子
     */
    @DeleteMapping("/delete")
    public R delete(
            @RequestParam Integer postId,
            @RequestParam Integer loginUserId
    ) {
        return postService.deletePost(postId, loginUserId);
    }

    /**
     * 接口4：帖子详情
     * 请求方式：GET
     * 参数：postId
     * 功能：根据id查询单条帖子完整数据
     */
    @GetMapping("/detail")
    public R detail(@RequestParam Integer postId) {
        return postService.postDetail(postId);
    }

    /**
     * 接口5：分页查询首页已审核帖子
     * 请求方式：GET
     * 参数：page(默认1)、size(默认10)
     * 功能：只查询status=1通过的帖子，按创建时间倒序
     */
    @GetMapping("/page")
    public R page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return postService.pageAllPassPost(page, size);
    }

    /**
     * 接口6：查询我的帖子
     * 请求方式：GET
     * 参数：userId
     * 功能：查询当前登录用户发布的所有帖子（含待审核/驳回）
     */
    @GetMapping("/user/list")
    public R userPost(@RequestParam Integer userId) {
        return postService.listUserPost(userId);
    }

}
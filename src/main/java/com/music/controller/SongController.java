package com.music.controller;

import com.music.common.R;
import com.music.model.request.CollectRequest;
import com.music.model.request.CommentLikeRequest;
import com.music.model.request.CommentRequest;
import com.music.model.request.SongRequest;
import com.music.service.CollectService;
import com.music.service.CommentLikeService;
import com.music.service.SongCommentService;
import com.music.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/song")
public class SongController {

    private final SongService songService;
    private final CollectService collectService;
    private final SongCommentService songCommentService;
    private final CommentLikeService commentLikeService;

    @PostMapping("/upload")
    public R uploadSong(

            SongRequest request,

            @RequestParam("songFile")
            MultipartFile songFile,

            @RequestParam(value="coverFile",required = false)
            MultipartFile coverFile){

        return songService.uploadSong(request,songFile,coverFile);

    }

    @PostMapping("/update")
    public R updateSong(
            SongRequest request,
            @RequestParam(value = "songFile", required = false) MultipartFile songFile,
            @RequestParam(value = "coverFile", required = false) MultipartFile coverFile
    ){
        return songService.updateSong(request, songFile, coverFile);
    }

    @DeleteMapping("/delete")
    public R deleteSong(Integer id){

        return songService.deleteSong(id);

    }

    @GetMapping("/user")
    public R userSongs(Integer userId){

        return songService.userSongs(userId);

    }

    @PostMapping("/detail")
    public R detail(Integer id){
        return songService.songDetail(id);
    }

    /**
     * 播放量+1（有效播放判定由前端触发）
     * POST /song/{id}/play
     */
    @PostMapping("/{id}/play")
    public R incrementPlay(@PathVariable Integer id) {
        return songService.incrementPlayCount(id);
    }

    // ====================== 歌曲收藏 ======================
    /**
     * 收藏 / 取消收藏歌曲
     * POST /song/collect
     * JSON请求体 CollectRequest
     */
    @PostMapping("/collect")
    public R collectSong(@RequestBody CollectRequest request) {
        return collectService.collectSong(request);
    }

    /**
     * 查询用户收藏的全部歌曲
     * GET /song/collect/list?userId=xx
     */
    @GetMapping("/collect/list")
    public R getUserCollect(@RequestParam Integer userId) {
        return collectService.getUserCollect(userId);
    }
// ====================== 歌曲评论 ======================
    /**
     * 发表歌曲评论/回复评论
     * POST /song/comment/add
     */
    @PostMapping("/comment/add")
    public R addComment(@RequestBody CommentRequest request) {
        return songCommentService.addSongComment(request);
    }

    /**
     * 删除歌曲评论
     * DELETE /song/comment/delete?commentId=1&userId=2
     */
    @DeleteMapping("/comment/delete")
    public R delComment(
            @RequestParam Integer commentId,
            @RequestParam Integer userId
    ) {
        return songCommentService.deleteSongComment(commentId, userId);
    }

    /**
     * 查询歌曲所有评论
     * GET /song/comment/list?songId=1&userId=2 (userId可选，用于判断是否已点赞)
     */
    @GetMapping("/comment/list")
    public R listComment(
            @RequestParam Integer songId,
            @RequestParam(required = false) Integer userId
    ) {
        return songCommentService.listSongComment(songId, userId);
    }

    /**
     * 歌曲评论点赞/取消点赞
     * POST /song/comment/like
     */
    @PostMapping("/comment/like")
    public R likeComment(@RequestBody CommentLikeRequest request) {
        request.setCommentType("song");
        return commentLikeService.likeComment(request);
    }

    @GetMapping("/hot")
    public R hotSongs(@RequestParam(defaultValue = "5") Integer limit) {
        return songService.hotSongs(limit);
    }

    /**
     * 按歌手用户ID获取已审核通过的作品（分页）
     * GET /song/singer-songs?singerUserId=&page=&size=
     */
    @GetMapping("/singer-songs")
    public R songsBySingerUserId(
            @RequestParam Integer singerUserId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return songService.songsBySingerUserId(singerUserId, page, size);
    }

}

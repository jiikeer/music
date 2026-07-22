package com.music.controller;

import com.music.common.R;
import com.music.model.request.CollectRequest;
import com.music.model.request.CommentRequest;
import com.music.model.request.SongRequest;
import com.music.service.CollectService;
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

    @PostMapping("/upload")
    public R uploadSong(
            SongRequest request,
            @RequestParam("songFile") MultipartFile songFile,
            @RequestParam(value="coverFile",required = false) MultipartFile coverFile,
            @RequestParam(value = "lyricFile", required = false) MultipartFile lyricFile
    ){
        return songService.uploadSong(request,songFile,coverFile,lyricFile);
    }

    @PostMapping("/update")
    public R updateSong(
            SongRequest request,
            @RequestParam(value = "songFile", required = false) MultipartFile songFile,
            @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
            @RequestParam(value = "lyricFile", required = false) MultipartFile lyricFile
    ){
        return songService.updateSong(request, songFile, coverFile, lyricFile);
    }

    @GetMapping("/hot")
    public R getHotSongList(@RequestParam(required = false, defaultValue = "5") Integer limit){
        return songService.getHotSongList(limit);
    }

    @DeleteMapping("/delete")
    public R deleteSong(Integer id){
        return songService.deleteSong(id);
    }

    @GetMapping("/user")
    public R userSongs(Integer userId){
        return songService.userSongs(userId);
    }

    @GetMapping("/detail")
    public R detail(Integer id){
        return songService.songDetail(id);
    }

    // ====================== 歌曲收藏 ======================
    @PostMapping("/collect")
    public R collectSong(@RequestBody CollectRequest request) {
        return collectService.collectSong(request);
    }

    @GetMapping("/collect/list")
    public R getUserCollect(@RequestParam Integer userId) {
        return collectService.getUserCollect(userId);
    }
    // ====================== 歌曲评论 ======================
    @PostMapping("/comment/add")
    public R addComment(@RequestBody CommentRequest request) {
        return songCommentService.addSongComment(request);
    }

    @DeleteMapping("/comment/delete")
    public R delComment(
            @RequestParam Integer commentId,
            @RequestParam Integer userId
    ) {
        return songCommentService.deleteSongComment(commentId, userId);
    }

    @GetMapping("/comment/list")
    public R listComment(@RequestParam Integer songId) {
        return songCommentService.listSongComment(songId);
    }

}
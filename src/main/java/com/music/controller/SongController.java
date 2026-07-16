package com.music.controller;

import com.music.common.R;
import com.music.model.request.SongRequest;
import com.music.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/song")
public class SongController {

    private final SongService songService;


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

    @GetMapping("/detail")
    public R detail(Integer id){

        return songService.songDetail(id);

    }

}

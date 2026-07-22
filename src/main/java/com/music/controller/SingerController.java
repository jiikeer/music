package com.music.controller;

import com.music.common.R;
import com.music.model.domain.Singer;
import com.music.service.SingerService;
import com.music.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/singer")
@RequiredArgsConstructor
public class SingerController {

    private final SingerService singerService;
    private final SongService songService;

    @GetMapping("")
    public R all() { return singerService.allSinger(); }

    @GetMapping("/page")
    public R page(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer size) {
        return singerService.pageSinger(page, size);
    }

    @GetMapping("/detail")
    public R detail(@RequestParam Integer id) { return singerService.singerDetail(id); }

    @PostMapping("/add")
    public R add(@RequestBody Singer singer) { return singerService.addSinger(singer); }

    @PostMapping("/update")
    public R update(@RequestBody Singer singer) { return singerService.updateSinger(singer); }

    @DeleteMapping("/delete")
    public R delete(@RequestParam Integer id) { return singerService.deleteSinger(id); }

    @PostMapping("/avatar/update")
    public R uploadAvatar(@RequestParam Integer id, @RequestParam("file") MultipartFile file) {
        return singerService.updateSingerAvatar(file, id);
    }

    @GetMapping("/{id}/songs")
    public R singerSongs(@PathVariable Integer id) {
        return songService.singerSongs(id);
    }
}

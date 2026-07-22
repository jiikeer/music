package com.music.controller;

import com.music.common.R;
import com.music.model.domain.SongSheet;
import com.music.service.SongSheetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/songSheet")
@RequiredArgsConstructor
public class SongSheetController {

    private final SongSheetService songSheetService;

    @GetMapping("")
    public R all() { return songSheetService.allSongSheet(); }

    @GetMapping("/page")
    public R page(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer size) {
        return songSheetService.pageSongSheet(page, size);
    }

    @GetMapping("/detail")
    public R detail(@RequestParam Integer id) { return songSheetService.songSheetDetail(id); }

    @PostMapping("/add")
    public R add(@RequestBody SongSheet songSheet) { return songSheetService.addSongSheet(songSheet); }

    @PostMapping("/update")
    public R update(@RequestBody SongSheet songSheet) { return songSheetService.updateSongSheet(songSheet); }

    @DeleteMapping("/delete")
    public R delete(@RequestParam Integer id) { return songSheetService.deleteSongSheet(id); }

    @PostMapping("/pic/update")
    public R uploadPic(@RequestParam Integer id, @RequestParam("file") MultipartFile file) {
        return songSheetService.updateSongSheetPic(file, id);
    }
}

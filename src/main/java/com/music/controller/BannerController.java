package com.music.controller;

import com.music.common.R;
import com.music.model.domain.Banner;
import com.music.service.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    @GetMapping("")
    public R all() { return bannerService.allBanner(); }

    @GetMapping("/page")
    public R page(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer size) {
        return bannerService.pageBanner(page, size);
    }

    @PostMapping("/add")
    public R add(@RequestBody Banner banner) { return bannerService.addBanner(banner); }

    @PostMapping("/update")
    public R update(@RequestBody Banner banner) { return bannerService.updateBanner(banner); }

    @DeleteMapping("/delete")
    public R delete(@RequestParam Integer id) { return bannerService.deleteBanner(id); }

    @PostMapping("/pic/update")
    public R uploadPic(@RequestParam Integer id, @RequestParam("file") MultipartFile file) {
        return bannerService.updateBannerPic(file, id);
    }
}

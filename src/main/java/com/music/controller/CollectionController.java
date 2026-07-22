package com.music.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.music.common.R;
import com.music.mapper.CollectMapper;
import com.music.model.domain.Collect;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/collection")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectMapper collectMapper;

    @GetMapping("")
    public R all() {
        return R.success(null, collectMapper.selectList(null));
    }

    @GetMapping("/page")
    public R page(@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "20") Integer size) {
        Page<Collect> pageInfo = new Page<>(page, size);
        collectMapper.selectPage(pageInfo, new QueryWrapper<Collect>().orderByDesc("create_time"));
        return R.success("查询成功", pageInfo);
    }

    @DeleteMapping("/delete")
    public R delete(@RequestParam Integer id) {
        collectMapper.deleteById(id);
        return R.success("删除成功");
    }
}

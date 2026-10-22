package com.shijian.controller;

import com.shijian.common.Result;
import com.shijian.service.RankService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/api/rank")
public class RankController {
    @Resource
    private RankService rankService;

    @GetMapping("/hot")
    public Result<?> hot(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(rankService.getHotPosts(limit));
    }

    @GetMapping("/weekly")
    public Result<?> weekly(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(rankService.getWeeklyRanking(limit));
    }

    @GetMapping("/monthly")
    public Result<?> monthly(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(rankService.getMonthlyRanking(limit));
    }

    @GetMapping("/category/{categoryId}")
    public Result<?> categoryRanking(@PathVariable Long categoryId,
                                     @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(rankService.getCategoryRanking(categoryId, limit));
    }
}

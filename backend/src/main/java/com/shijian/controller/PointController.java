package com.shijian.controller;

import com.shijian.common.Result;
import com.shijian.service.PointService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/point")
public class PointController {
    @Resource
    private PointService pointService;

    @PostMapping("/checkin")
    public Result<?> checkin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return pointService.checkin(userId);
    }

    @GetMapping("/checkin/status")
    public Result<?> checkinStatus(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(pointService.hasCheckedInToday(userId));
    }

    @GetMapping("/my")
    public Result<?> myPoints(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(pointService.getUserPointsAndLevel(userId));
    }

    @GetMapping("/records")
    public Result<?> records(@RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "20") Integer size,
                             HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(pointService.getPointRecords(userId, page, size));
    }

    @GetMapping("/rank")
    public Result<?> rank(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(pointService.getPointsRank(page, size));
    }
}

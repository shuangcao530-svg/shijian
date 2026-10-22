package com.shijian.controller;

import com.shijian.common.Result;
import com.shijian.service.LikeService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/like")
public class LikeController {
    @Resource
    private LikeService likeService;

    @PostMapping("/post/{postId}")
    public Result<Void> likePost(@PathVariable Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return likeService.likePost(userId, postId);
    }

    @DeleteMapping("/post/{postId}")
    public Result<Void> unlikePost(@PathVariable Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return likeService.unlikePost(userId, postId);
    }
}

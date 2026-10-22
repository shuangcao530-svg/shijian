package com.shijian.controller;

import com.shijian.common.Result;
import com.shijian.dto.PostQueryDTO;
import com.shijian.service.TopicService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/topic")
public class TopicController {
    @Resource
    private TopicService topicService;

    @GetMapping
    public Result<?> listAll() {
        return Result.success(topicService.listAll());
    }

    @GetMapping("/hot")
    public Result<?> listHot(@RequestParam(defaultValue = "6") Integer limit) {
        return Result.success(topicService.listHot(limit));
    }

    @PostMapping("/{id}/follow")
    public Result<?> follow(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return topicService.follow(userId, id);
    }

    @DeleteMapping("/{id}/follow")
    public Result<?> unfollow(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return topicService.unfollow(userId, id);
    }

    @GetMapping("/my-follows")
    public Result<?> myFollows(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(topicService.getUserFollowedTopics(userId));
    }

    @GetMapping("/{id}/posts")
    public Result<?> topicPosts(@PathVariable Long id, PostQueryDTO query, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(topicService.getTopicPosts(id, query, userId));
    }

    @GetMapping("/{id}/check-follow")
    public Result<?> checkFollow(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(topicService.isFollowed(userId, id));
    }
}

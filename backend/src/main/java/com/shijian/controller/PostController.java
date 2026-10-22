package com.shijian.controller;

import com.shijian.common.Result;
import com.shijian.dto.PostDTO;
import com.shijian.dto.PostQueryDTO;
import com.shijian.service.PostService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/post")
public class PostController {
    @Resource
    private PostService postService;

    @PostMapping
    public Result<?> create(@Valid @RequestBody PostDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return postService.create(userId, dto);
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return postService.getById(id, userId);
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody PostDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return postService.update(userId, id, dto);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return postService.delete(userId, id);
    }

    @GetMapping
    public Result<?> page(PostQueryDTO query, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(postService.page(query, userId));
    }

    @GetMapping("/my")
    public Result<?> myPosts(@RequestParam(defaultValue = "1") Integer page,
                             @RequestParam(defaultValue = "10") Integer size,
                             HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(postService.getMyPosts(userId, page, size));
    }

    @GetMapping("/user/{userId}")
    public Result<?> userPosts(@PathVariable Long userId,
                               @RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size,
                               HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        return Result.success(postService.getUserPosts(currentUserId, userId, page, size));
    }

    @GetMapping("/friends")
    public Result<?> friendPosts(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size,
                                 HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(postService.getFriendPosts(userId, page, size));
    }
}

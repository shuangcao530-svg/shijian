package com.shijian.controller;

import com.shijian.common.Result;
import com.shijian.dto.CommentDTO;
import com.shijian.service.CommentService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/comment")
public class CommentController {
    @Resource
    private CommentService commentService;

    @PostMapping
    public Result<?> create(@Valid @RequestBody CommentDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return commentService.create(userId, dto);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return commentService.delete(userId, id);
    }

    @PostMapping("/{id}/like")
    public Result<Void> like(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return commentService.like(userId, id);
    }

    @DeleteMapping("/{id}/like")
    public Result<Void> unlike(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return commentService.unlike(userId, id);
    }

    @GetMapping("/post/{postId}")
    public Result<?> getByPostId(@PathVariable Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(commentService.getByPostId(postId, userId));
    }
}

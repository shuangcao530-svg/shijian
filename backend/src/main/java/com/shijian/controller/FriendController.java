package com.shijian.controller;

import com.shijian.common.Result;
import com.shijian.dto.FriendRequestDTO;
import com.shijian.service.FriendService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/friend")
public class FriendController {
    @Resource
    private FriendService friendService;

    @PostMapping("/request")
    public Result<Void> sendRequest(@RequestBody FriendRequestDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return friendService.sendRequest(userId, dto.getToUserId(), dto.getMessage());
    }

    @PutMapping("/request/{id}")
    public Result<Void> handleRequest(@PathVariable Long id,
                                      @RequestParam String action,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return friendService.handleRequest(userId, id, action);
    }

    @GetMapping("/requests")
    public Result<?> getRequests(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(friendService.getFriendRequests(userId));
    }

    @DeleteMapping("/{friendId}")
    public Result<Void> deleteFriend(@PathVariable Long friendId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return friendService.deleteFriend(userId, friendId);
    }

    @GetMapping
    public Result<?> getFriends(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "20") Integer size,
                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(friendService.getFriends(userId, page, size));
    }

    @PostMapping("/blacklist/{blockedUserId}")
    public Result<Void> addToBlacklist(@PathVariable Long blockedUserId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return friendService.addToBlacklist(userId, blockedUserId);
    }

    @DeleteMapping("/blacklist/{blockedUserId}")
    public Result<Void> removeFromBlacklist(@PathVariable Long blockedUserId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return friendService.removeFromBlacklist(userId, blockedUserId);
    }

    @GetMapping("/blacklist")
    public Result<?> getBlacklist(@RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "20") Integer size,
                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(friendService.getBlacklist(userId, page, size));
    }
}

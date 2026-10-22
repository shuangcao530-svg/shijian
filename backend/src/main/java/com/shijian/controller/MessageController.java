package com.shijian.controller;

import com.shijian.common.Result;
import com.shijian.service.MessageService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class MessageController {
    @Resource
    private MessageService messageService;

    @PostMapping
    public Result<?> send(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long toUserId = Long.parseLong(body.get("toUserId"));
        return messageService.send(userId, toUserId, body.get("content"));
    }

    @GetMapping("/conversations")
    public Result<?> conversations(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(messageService.getConversations(userId));
    }

    @GetMapping("/with/{friendId}")
    public Result<?> getMessages(@PathVariable Long friendId,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "50") Integer size,
                                  HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(messageService.getMessages(userId, friendId, page, size));
    }

    @PutMapping("/read/{friendId}")
    public Result<Void> markRead(@PathVariable Long friendId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return messageService.markRead(userId, friendId);
    }

    @GetMapping("/unread")
    public Result<?> unreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(messageService.getUnreadCount(userId));
    }
}

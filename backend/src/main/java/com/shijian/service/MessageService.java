package com.shijian.service;

import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.entity.Message;
import java.util.List;
import java.util.Map;

public interface MessageService {
    Result<Message> send(Long fromUserId, Long toUserId, String content);
    List<Map<String, Object>> getConversations(Long userId);
    PageResult<Message> getMessages(Long userId, Long friendId, Integer page, Integer size);
    Result<Void> markRead(Long userId, Long fromUserId);
    int getUnreadCount(Long userId);
}

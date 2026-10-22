package com.shijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.entity.Message;
import com.shijian.entity.User;
import com.shijian.mapper.MessageMapper;
import com.shijian.mapper.UserMapper;
import com.shijian.service.MessageService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private UserMapper userMapper;

    @Override
    public Result<Message> send(Long fromUserId, Long toUserId, String content) {
        if (content == null || content.trim().isEmpty()) {
            return Result.error("消息不能为空");
        }
        Message msg = new Message();
        msg.setFromUserId(fromUserId);
        msg.setToUserId(toUserId);
        msg.setContent(content.trim());
        msg.setIsRead(0);
        messageMapper.insert(msg);
        return Result.success("发送成功", msg);
    }

    @Override
    public List<Map<String, Object>> getConversations(Long userId) {
        // 获取所有与userId有关的消息
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
        w.eq(Message::getToUserId, userId).or().eq(Message::getFromUserId, userId)
         .orderByDesc(Message::getCreateTime);
        List<Message> all = messageMapper.selectList(w);

        // 按对话对象分组
        Map<Long, List<Message>> grouped = new HashMap<>();
        for (Message m : all) {
            Long otherId = m.getFromUserId().equals(userId) ? m.getToUserId() : m.getFromUserId();
            grouped.computeIfAbsent(otherId, k -> new ArrayList<>()).add(m);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, List<Message>> e : grouped.entrySet()) {
            Long otherId = e.getKey();
            List<Message> msgs = e.getValue();
            Message latest = msgs.get(0);
            long unread = msgs.stream()
                    .filter(m -> m.getToUserId().equals(userId) && m.getIsRead() == 0)
                    .count();

            User other = userMapper.selectById(otherId);
            Map<String, Object> conv = new HashMap<>();
            conv.put("userId", otherId);
            conv.put("nickname", other != null ? (other.getNickname() != null ? other.getNickname() : other.getUsername()) : "未知");
            conv.put("avatar", other != null ? other.getAvatar() : null);
            conv.put("lastMsg", latest.getContent().length() > 50 ? latest.getContent().substring(0, 50) + "..." : latest.getContent());
            conv.put("time", latest.getCreateTime());
            conv.put("unread", unread);
            conv.put("isMe", latest.getFromUserId().equals(userId));
            result.add(conv);
        }
        result.sort((a, b) -> ((java.time.LocalDateTime) b.get("time")).compareTo((java.time.LocalDateTime) a.get("time")));
        return result;
    }

    @Override
    public PageResult<Message> getMessages(Long userId, Long friendId, Integer page, Integer size) {
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
        w.and(wr -> wr.eq(Message::getFromUserId, userId).eq(Message::getToUserId, friendId)
                .or().eq(Message::getFromUserId, friendId).eq(Message::getToUserId, userId))
         .orderByDesc(Message::getCreateTime);
        Page<Message> p = messageMapper.selectPage(new Page<>(page, size), w);
        // Reverse to show oldest first
        List<Message> reversed = new ArrayList<>(p.getRecords());
        Collections.reverse(reversed);
        return PageResult.of(p.getTotal(), page, size, reversed);
    }

    @Override
    public Result<Void> markRead(Long userId, Long fromUserId) {
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
        w.eq(Message::getToUserId, userId).eq(Message::getFromUserId, fromUserId).eq(Message::getIsRead, 0);
        List<Message> unread = messageMapper.selectList(w);
        for (Message m : unread) {
            m.setIsRead(1);
            messageMapper.updateById(m);
        }
        return Result.success(null);
    }

    @Override
    public int getUnreadCount(Long userId) {
        LambdaQueryWrapper<Message> w = new LambdaQueryWrapper<>();
        w.eq(Message::getToUserId, userId).eq(Message::getIsRead, 0);
        Long count = messageMapper.selectCount(w);
        return count != null ? count.intValue() : 0;
    }
}

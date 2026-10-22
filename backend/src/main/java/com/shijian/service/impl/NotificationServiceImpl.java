package com.shijian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.entity.Notification;
import com.shijian.mapper.NotificationMapper;
import com.shijian.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    @Resource
    private NotificationMapper notificationMapper;

    @Override
    @Transactional
    public void create(Long userId, Long fromUserId, String type, String targetType, Long targetId, String content) {
        // 不给自己发通知
        if (userId.equals(fromUserId)) return;
        Notification n = new Notification();
        n.setUserId(userId);
        n.setFromUserId(fromUserId);
        n.setType(type);
        n.setTargetType(targetType);
        n.setTargetId(targetId);
        n.setContent(content);
        n.setIsRead(0);
        notificationMapper.insert(n);
    }

    @Override
    public PageResult<Notification> getUserNotifications(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime);
        Page<Notification> p = notificationMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(p.getTotal(), page, size, p.getRecords());
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0)).intValue();
    }

    @Override
    public Result<Void> markAsRead(Long userId, Long notificationId) {
        Notification n = notificationMapper.selectById(notificationId);
        if (n == null || !n.getUserId().equals(userId)) {
            return Result.error("通知不存在");
        }
        n.setIsRead(1);
        notificationMapper.updateById(n);
        return Result.success("已读", null);
    }

    @Override
    @Transactional
    public Result<Void> markAllAsRead(Long userId) {
        List<Notification> unread = notificationMapper.selectList(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsRead, 0));
        for (Notification n : unread) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
        return Result.success("全部已读", null);
    }

    @Override
    public Result<Void> delete(Long userId, Long notificationId) {
        Notification n = notificationMapper.selectById(notificationId);
        if (n == null || !n.getUserId().equals(userId)) {
            return Result.error("通知不存在");
        }
        notificationMapper.deleteById(notificationId);
        return Result.success("删除成功", null);
    }
}

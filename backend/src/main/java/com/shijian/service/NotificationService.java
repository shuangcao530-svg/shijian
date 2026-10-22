package com.shijian.service;

import com.shijian.common.PageResult;
import com.shijian.common.Result;
import com.shijian.entity.Notification;

public interface NotificationService {
    /** 创建通知 */
    void create(Long userId, Long fromUserId, String type, String targetType, Long targetId, String content);

    /** 用户的通知列表 */
    PageResult<Notification> getUserNotifications(Long userId, Integer page, Integer size);

    /** 未读数量 */
    int getUnreadCount(Long userId);

    /** 标记已读 */
    Result<Void> markAsRead(Long userId, Long notificationId);

    /** 全部标记已读 */
    Result<Void> markAllAsRead(Long userId);

    /** 删除通知 */
    Result<Void> delete(Long userId, Long notificationId);
}

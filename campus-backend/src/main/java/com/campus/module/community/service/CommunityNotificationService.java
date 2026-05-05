package com.campus.module.community.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.community.entity.CommunityNotification;

/**
 * 社区互动通知服务
 */
public interface CommunityNotificationService extends IService<CommunityNotification> {

    void notifyPostReply(Long postAuthorId, Long fromUserId, Long postId, String contentSummary);

    void notifyCommentReply(Long replyAuthorId, Long fromUserId, Long postId, Long replyId, String contentSummary);

    IPage<CommunityNotification> listNotifications(Long userId, Integer page, Integer size);

    int getUnreadCount(Long userId);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);
}

package com.campus.module.community.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.community.entity.CommunityNotification;

/**
 * 社区互动通知服务
 */
public interface CommunityNotificationService extends IService<CommunityNotification> {

    /**
     * 生成通知：帖子收到新评论
     * @param postAuthorId 帖子作者ID
     * @param fromUserId 评论者ID
     * @param postId 帖子ID
     * @param contentSummary 评论内容摘要
     */
    void notifyPostReply(Long postAuthorId, Long fromUserId, Long postId, String contentSummary);

    /**
     * 生成通知：评论收到新回复
     * @param replyAuthorId 被回复者ID
     * @param fromUserId 回复者ID
     * @param postId 帖子ID
     * @param replyId 评论ID
     * @param contentSummary 回复内容摘要
     */
    void notifyCommentReply(Long replyAuthorId, Long fromUserId, Long postId, Long replyId, String contentSummary);

    /**
     * 查询用户的通知列表（分页）
     */
    IPage<CommunityNotification> listNotifications(Long userId, Integer page, Integer size);

    /**
     * 获取用户未读通知数
     */
    int getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead(Long userId);
}

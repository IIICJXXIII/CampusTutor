package com.campus.module.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.module.community.entity.CommunityNotification;
import com.campus.module.community.entity.CommunityPost;
import com.campus.module.community.mapper.CommunityNotificationMapper;
import com.campus.module.community.mapper.CommunityPostMapper;
import com.campus.module.community.service.CommunityNotificationService;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 社区互动通知服务实现
 * 当用户帖子被评论或评论被回复时自动生成通知
 */
@Service
@RequiredArgsConstructor
public class CommunityNotificationServiceImpl
        extends ServiceImpl<CommunityNotificationMapper, CommunityNotification>
        implements CommunityNotificationService {

    private final SysUserMapper sysUserMapper;
    private final CommunityPostMapper communityPostMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notifyPostReply(Long postAuthorId, Long fromUserId, Long postId, String contentSummary) {
        if (postAuthorId.equals(fromUserId)) return; // 不通知自己的操作

        CommunityNotification notification = new CommunityNotification();
        notification.setUserId(postAuthorId);
        notification.setType(1);
        notification.setPostId(postId);
        notification.setFromUserId(fromUserId);
        notification.setContentSummary(truncateSummary(contentSummary));
        notification.setIsRead(0);
        save(notification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void notifyCommentReply(Long replyAuthorId, Long fromUserId, Long postId, Long replyId, String contentSummary) {
        if (replyAuthorId.equals(fromUserId)) return; // 不通知自己的操作

        CommunityNotification notification = new CommunityNotification();
        notification.setUserId(replyAuthorId);
        notification.setType(2);
        notification.setPostId(postId);
        notification.setReplyId(replyId);
        notification.setFromUserId(fromUserId);
        notification.setContentSummary(truncateSummary(contentSummary));
        notification.setIsRead(0);
        save(notification);
    }

    @Override
    public IPage<CommunityNotification> listNotifications(Long userId, Integer page, Integer size) {
        Page<CommunityNotification> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CommunityNotification> wrapper = new LambdaQueryWrapper<CommunityNotification>()
                .eq(CommunityNotification::getUserId, userId)
                .orderByDesc(CommunityNotification::getCreateTime);
        IPage<CommunityNotification> result = page(pageParam, wrapper);
        // 填充关联信息
        for (CommunityNotification n : result.getRecords()) {
            fillNotificationInfo(n);
        }
        return result;
    }

    @Override
    public int getUnreadCount(Long userId) {
        return Math.toIntExact(count(new LambdaQueryWrapper<CommunityNotification>()
                .eq(CommunityNotification::getUserId, userId)
                .eq(CommunityNotification::getIsRead, 0)));
    }

    @Override
    public void markAsRead(Long notificationId, Long userId) {
        update(new LambdaUpdateWrapper<CommunityNotification>()
                .eq(CommunityNotification::getId, notificationId)
                .eq(CommunityNotification::getUserId, userId)
                .set(CommunityNotification::getIsRead, 1));
    }

    @Override
    public void markAllAsRead(Long userId) {
        update(new LambdaUpdateWrapper<CommunityNotification>()
                .eq(CommunityNotification::getUserId, userId)
                .eq(CommunityNotification::getIsRead, 0)
                .set(CommunityNotification::getIsRead, 1));
    }

    private void fillNotificationInfo(CommunityNotification notification) {
        // 填充触发者信息
        SysUser fromUser = sysUserMapper.selectById(notification.getFromUserId());
        if (fromUser != null) {
            notification.setFromUserNickname(fromUser.getNickname());
            notification.setFromUserAvatar(fromUser.getAvatarUrl());
        }
        // 填充帖子标题
        CommunityPost post = communityPostMapper.selectById(notification.getPostId());
        if (post != null) {
            notification.setPostTitle(post.getTitle());
        }
    }

    private String truncateSummary(String content) {
        if (content == null) return "";
        return content.length() > 200 ? content.substring(0, 197) + "..." : content;
    }
}

package com.campus.module.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.context.UserContext;
import com.campus.common.exception.BusinessException;
import com.campus.module.community.entity.CommunityCommentLike;
import com.campus.module.community.entity.CommunityReply;
import com.campus.module.community.mapper.CommunityCommentLikeMapper;
import com.campus.module.community.mapper.CommunityReplyMapper;
import com.campus.module.community.service.CommunityReplyService;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityReplyServiceImpl extends ServiceImpl<CommunityReplyMapper, CommunityReply>
        implements CommunityReplyService {

    private final SysUserService sysUserService;
    private final CommunityCommentLikeMapper commentLikeMapper;

    @Override
    public IPage<CommunityReply> listReplies(Long postId, Integer page, Integer size) {
        Page<CommunityReply> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CommunityReply> wrapper = new LambdaQueryWrapper<CommunityReply>()
                .eq(CommunityReply::getPostId, postId)
                .eq(CommunityReply::getRootId, 0)
                .eq(CommunityReply::getStatus, 1)
                .orderByDesc(CommunityReply::getCreateTime);
        IPage<CommunityReply> result = page(pageParam, wrapper);
        result.getRecords().forEach(this::fillReplyInfo);
        fillRepliesLikedStatus(result.getRecords());
        return result;
    }

    @Override
    public IPage<CommunityReply> listSubReplies(Long rootId, Long lastId, Integer size) {
        Page<CommunityReply> pageParam = new Page<>(1, size);
        LambdaQueryWrapper<CommunityReply> wrapper = new LambdaQueryWrapper<CommunityReply>()
                .eq(CommunityReply::getRootId, rootId)
                .eq(CommunityReply::getStatus, 1)
                .orderByAsc(CommunityReply::getCreateTime);
        if (lastId != null && lastId > 0) {
            wrapper.gt(CommunityReply::getId, lastId);
        }
        IPage<CommunityReply> result = page(pageParam, wrapper);
        result.getRecords().forEach(this::fillReplyInfo);
        fillRepliesLikedStatus(result.getRecords());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CommunityReply createReply(Long userId, CommunityReply reply) {
        reply.setUserId(userId);
        reply.setStatus(1);
        reply.setLikeCount(0);
        reply.setReplyCount(0);

        if (reply.getRootId() == null) {
            reply.setRootId(0L);
        }
        if (reply.getParentId() == null) {
            reply.setParentId(0L);
        }
        if (reply.getReplyToId() == null) {
            reply.setReplyToId(0L);
        }

        if (reply.getRootId() > 0) {
            CommunityReply rootReply = getById(reply.getRootId());
            if (rootReply == null || !rootReply.getPostId().equals(reply.getPostId())) {
                throw new BusinessException("回复的评论不存在");
            }
            if (reply.getReplyToUserId() != null) {
                SysUser replyToUser = sysUserService.getById(reply.getReplyToUserId());
                if (replyToUser != null) {
                    reply.setReplyToNickname(replyToUser.getNickname());
                }
            }
        } else {
            reply.setRootId(0L);
            reply.setParentId(0L);
            reply.setReplyToId(0L);
        }

        save(reply);

        if (reply.getRootId() > 0) {
            update(new LambdaUpdateWrapper<CommunityReply>()
                    .eq(CommunityReply::getId, reply.getRootId())
                    .setSql("reply_count = reply_count + 1"));
        }

        fillReplyInfo(reply);
        return reply;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReply(Long replyId) {
        Long userId = UserContext.getUserId();
        CommunityReply reply = getById(replyId);
        if (reply == null) {
            throw new BusinessException("评论不存在");
        }
        if (!reply.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的评论");
        }

        reply.setStatus(3);
        updateById(reply);

        if (reply.getRootId() > 0) {
            update(new LambdaUpdateWrapper<CommunityReply>()
                    .eq(CommunityReply::getId, reply.getRootId())
                    .setSql("reply_count = GREATEST(reply_count - 1, 0)"));
        }

        if (reply.getRootId() == 0) {
            update(new LambdaUpdateWrapper<CommunityReply>()
                    .eq(CommunityReply::getRootId, replyId)
                    .ne(CommunityReply::getStatus, 3)
                    .set(CommunityReply::getStatus, 3));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeReply(Long replyId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }

        CommunityCommentLike existing = commentLikeMapper.selectOne(
                new LambdaQueryWrapper<CommunityCommentLike>()
                        .eq(CommunityCommentLike::getCommentId, replyId)
                        .eq(CommunityCommentLike::getUserId, userId));

        if (existing != null) {
            commentLikeMapper.deleteById(existing.getId());
            update(new LambdaUpdateWrapper<CommunityReply>()
                    .eq(CommunityReply::getId, replyId)
                    .setSql("like_count = GREATEST(like_count - 1, 0)"));
            return false;
        } else {
            CommunityCommentLike like = new CommunityCommentLike();
            like.setCommentId(replyId);
            like.setUserId(userId);
            commentLikeMapper.insert(like);
            update(new LambdaUpdateWrapper<CommunityReply>()
                    .eq(CommunityReply::getId, replyId)
                    .setSql("like_count = like_count + 1"));
            return true;
        }
    }

    private void fillReplyInfo(CommunityReply reply) {
        SysUser user = sysUserService.getById(reply.getUserId());
        if (user != null) {
            reply.setAuthorNickname(user.getNickname());
            reply.setAuthorAvatar(user.getAvatarUrl());
        }
        if (reply.getReplyToUserId() != null && reply.getReplyToUserId() > 0) {
            SysUser replyToUser = sysUserService.getById(reply.getReplyToUserId());
            if (replyToUser != null) {
                reply.setReplyToNickname(replyToUser.getNickname());
            }
        }
    }

    private void fillRepliesLikedStatus(List<CommunityReply> replies) {
        Long userId = UserContext.getUserId();
        if (userId == null || replies.isEmpty()) {
            replies.forEach(r -> r.setLiked(false));
            return;
        }
        Set<Long> likedCommentIds = commentLikeMapper.selectList(
                        new LambdaQueryWrapper<CommunityCommentLike>()
                                .eq(CommunityCommentLike::getUserId, userId)
                                .in(CommunityCommentLike::getCommentId,
                                        replies.stream().map(CommunityReply::getId).collect(Collectors.toList())))
                .stream()
                .map(CommunityCommentLike::getCommentId)
                .collect(Collectors.toSet());
        replies.forEach(r -> r.setLiked(likedCommentIds.contains(r.getId())));
    }
}

package com.campus.module.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.exception.BusinessException;
import com.campus.module.community.entity.CommunityReply;
import com.campus.module.community.mapper.CommunityReplyMapper;
import com.campus.module.community.service.CommunityReplyService;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityReplyServiceImpl extends ServiceImpl<CommunityReplyMapper, CommunityReply>
        implements CommunityReplyService {

    private final SysUserService sysUserService;

    @Override
    public Long createReply(Long userId, Long postId, String content) {
        CommunityReply reply = new CommunityReply();
        reply.setPostId(postId);
        reply.setUserId(userId);
        reply.setContent(content);
        save(reply);
        return reply.getId();
    }

    @Override
    public IPage<CommunityReply> listReplies(Long postId, Integer page, Integer size) {
        Page<CommunityReply> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CommunityReply> wrapper = new LambdaQueryWrapper<CommunityReply>()
                .eq(CommunityReply::getPostId, postId)
                .orderByAsc(CommunityReply::getCreateTime);
        IPage<CommunityReply> result = page(pageParam, wrapper);
        fillReplyExtraInfo(result.getRecords());
        return result;
    }

    private void fillReplyExtraInfo(List<CommunityReply> replies) {
        if (replies == null || replies.isEmpty())
            return;

        Set<Long> userIds = replies.stream().map(CommunityReply::getUserId).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = sysUserService.listByIds(userIds)
                .stream().collect(Collectors.toMap(SysUser::getId, u -> u));

        for (CommunityReply reply : replies) {
            SysUser user = userMap.get(reply.getUserId());
            if (user != null) {
                reply.setAuthorName(user.getNickname());
                reply.setAuthorAvatar(user.getAvatarUrl());
            }
        }
    }
}

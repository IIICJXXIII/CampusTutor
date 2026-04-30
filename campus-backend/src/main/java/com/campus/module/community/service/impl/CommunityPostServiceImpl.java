package com.campus.module.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.exception.BusinessException;
import com.campus.module.community.entity.CommunityPost;
import com.campus.module.community.entity.CommunityReply;
import com.campus.module.community.mapper.CommunityPostMapper;
import com.campus.module.community.mapper.CommunityReplyMapper;
import com.campus.module.community.service.CommunityPostService;
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
public class CommunityPostServiceImpl extends ServiceImpl<CommunityPostMapper, CommunityPost>
        implements CommunityPostService {

    private final CommunityReplyMapper replyMapper;
    private final SysUserService sysUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(Long userId, String title, String content, Integer topicType) {
        CommunityPost post = new CommunityPost();
        post.setUserId(userId);
        post.setTitle(title);
        post.setContent(content);
        post.setTopicType(topicType != null ? topicType : 1);
        post.setViewCount(0);
        post.setLikeCount(0);
        save(post);
        return post.getId();
    }

    @Override
    public IPage<CommunityPost> listPosts(Integer topicType, Integer page, Integer size) {
        Page<CommunityPost> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<CommunityPost>()
                .eq(topicType != null, CommunityPost::getTopicType, topicType)
                .orderByDesc(CommunityPost::getCreateTime);
        IPage<CommunityPost> result = page(pageParam, wrapper);
        fillPostExtraInfo(result.getRecords());
        return result;
    }

    @Override
    public CommunityPost getPostDetail(Long postId) {
        CommunityPost post = getById(postId);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        post.setViewCount(post.getViewCount() + 1);
        updateById(post);

        SysUser user = sysUserService.getById(post.getUserId());
        if (user != null) {
            post.setAuthorName(user.getNickname());
            post.setAuthorAvatar(user.getAvatarUrl());
        }

        LambdaQueryWrapper<CommunityReply> replyWrapper = new LambdaQueryWrapper<CommunityReply>()
                .eq(CommunityReply::getPostId, postId);
        post.setReplyCount(replyMapper.selectCount(replyWrapper).intValue());

        return post;
    }

    @Override
    public void likePost(Long userId, Long postId) {
        CommunityPost post = getById(postId);
        if (post == null) {
            throw new BusinessException(404, "帖子不存在");
        }
        post.setLikeCount(post.getLikeCount() + 1);
        updateById(post);
    }

    private void fillPostExtraInfo(List<CommunityPost> posts) {
        if (posts == null || posts.isEmpty())
            return;

        Set<Long> userIds = posts.stream().map(CommunityPost::getUserId).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = sysUserService.listByIds(userIds)
                .stream().collect(Collectors.toMap(SysUser::getId, u -> u));

        for (CommunityPost post : posts) {
            SysUser user = userMap.get(post.getUserId());
            if (user != null) {
                post.setAuthorName(user.getNickname());
                post.setAuthorAvatar(user.getAvatarUrl());
            }
            LambdaQueryWrapper<CommunityReply> replyWrapper = new LambdaQueryWrapper<CommunityReply>()
                    .eq(CommunityReply::getPostId, post.getId());
            post.setReplyCount(replyMapper.selectCount(replyWrapper).intValue());
        }
    }
}

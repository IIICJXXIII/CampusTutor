package com.campus.module.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.context.UserContext;
import com.campus.common.exception.BusinessException;
import com.campus.common.result.ResultCode;
import com.campus.module.community.dto.CommunityPostRequest;
import com.campus.module.community.entity.CommunityPost;
import com.campus.module.community.entity.CommunityPostLike;
import com.campus.module.community.mapper.CommunityPostLikeMapper;
import com.campus.module.community.mapper.CommunityPostMapper;
import com.campus.module.community.mapper.CommunityReplyMapper;
import com.campus.module.community.service.CommunityPostService;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityPostServiceImpl extends ServiceImpl<CommunityPostMapper, CommunityPost>
        implements CommunityPostService {

    private final CommunityReplyMapper replyMapper;
    private final CommunityPostLikeMapper postLikeMapper;
    private final SysUserService sysUserService;

    @Override
    public IPage<CommunityPost> listPosts(Integer topicType, Integer page, Integer size) {
        Page<CommunityPost> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<CommunityPost>();
        if (topicType != null) {
            wrapper.eq(CommunityPost::getTopicType, topicType);
        }
        wrapper.eq(CommunityPost::getStatus, 1);
        wrapper.orderByDesc(CommunityPost::getCreateTime);
        IPage<CommunityPost> result = page(pageParam, wrapper);
        result.getRecords().forEach(this::fillPostInfo);
        fillPostsLikedStatus(result.getRecords());
        return result;
    }

    @Override
    public IPage<CommunityPost> adminListPosts(Integer topicType, Integer page, Integer size) {
        Page<CommunityPost> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CommunityPost> wrapper = new LambdaQueryWrapper<CommunityPost>();
        if (topicType != null) {
            wrapper.eq(CommunityPost::getTopicType, topicType);
        }
        wrapper.orderByDesc(CommunityPost::getCreateTime);
        IPage<CommunityPost> result = page(pageParam, wrapper);
        result.getRecords().forEach(this::fillPostInfo);
        return result;
    }

    @Override
    public CommunityPost getPostDetail(Long id) {
        CommunityPost post = getById(id);
        if (post == null || !Integer.valueOf(1).equals(post.getStatus())) {
            throw new BusinessException("帖子不存在");
        }
        update(new LambdaUpdateWrapper<CommunityPost>()
                .eq(CommunityPost::getId, id)
                .setSql("view_count = view_count + 1"));
        post.setViewCount(post.getViewCount() + 1);
        fillPostInfo(post);
        fillPostsLikedStatus(List.of(post));
        return post;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPost(Long userId, CommunityPostRequest request) {
        CommunityPost post = new CommunityPost();
        post.setUserId(userId);
        post.setTitle(request.getTitle().trim());
        post.setContent(request.getContent().trim());
        post.setTopicType(request.getTopicType());
        post.setTags(request.getTags() != null ? request.getTags().trim() : null);
        post.setImages(request.getImages());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setStatus(1);
        save(post);
        return post.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long id) {
        CommunityPost post = getById(id);
        if (post == null) {
            throw new BusinessException(ResultCode.POST_NOT_FOUND.getCode(), ResultCode.POST_NOT_FOUND.getMsg());
        }
        update(new LambdaUpdateWrapper<CommunityPost>()
                .eq(CommunityPost::getId, id)
                .set(CommunityPost::getStatus, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likePost(Long id) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }

        CommunityPostLike existing = postLikeMapper.selectOne(
                new LambdaQueryWrapper<CommunityPostLike>()
                        .eq(CommunityPostLike::getPostId, id)
                        .eq(CommunityPostLike::getUserId, userId));

        if (existing != null) {
            postLikeMapper.deleteById(existing.getId());
            update(new LambdaUpdateWrapper<CommunityPost>()
                    .eq(CommunityPost::getId, id)
                    .setSql("like_count = GREATEST(like_count - 1, 0)"));
            return false;
        } else {
            CommunityPostLike like = new CommunityPostLike();
            like.setPostId(id);
            like.setUserId(userId);
            postLikeMapper.insert(like);
            update(new LambdaUpdateWrapper<CommunityPost>()
                    .eq(CommunityPost::getId, id)
                    .setSql("like_count = like_count + 1"));
            return true;
        }
    }

    private void fillPostInfo(CommunityPost post) {
        SysUser user = sysUserService.getById(post.getUserId());
        if (user != null) {
            post.setAuthorNickname(user.getNickname());
            post.setAuthorAvatar(user.getAvatarUrl());
        }
        Long replyCount = replyMapper.selectCount(
                new LambdaQueryWrapper<com.campus.module.community.entity.CommunityReply>()
                        .eq(com.campus.module.community.entity.CommunityReply::getPostId, post.getId())
                        .eq(com.campus.module.community.entity.CommunityReply::getRootId, 0)
                        .eq(com.campus.module.community.entity.CommunityReply::getStatus, 1));
        post.setReplyCount(replyCount.intValue());
    }

    private void fillPostsLikedStatus(List<CommunityPost> posts) {
        Long userId = UserContext.getUserId();
        if (userId == null || posts.isEmpty()) {
            posts.forEach(p -> p.setLiked(false));
            return;
        }
        Set<Long> likedPostIds = postLikeMapper.selectList(
                        new LambdaQueryWrapper<CommunityPostLike>()
                                .eq(CommunityPostLike::getUserId, userId)
                                .in(CommunityPostLike::getPostId,
                                        posts.stream().map(CommunityPost::getId).collect(Collectors.toList())))
                .stream()
                .map(CommunityPostLike::getPostId)
                .collect(Collectors.toSet());
        posts.forEach(p -> p.setLiked(likedPostIds.contains(p.getId())));
    }
}

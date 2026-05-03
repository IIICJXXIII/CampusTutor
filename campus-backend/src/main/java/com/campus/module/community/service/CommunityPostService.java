package com.campus.module.community.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.community.dto.CommunityPostRequest;
import com.campus.module.community.entity.CommunityPost;

public interface CommunityPostService extends IService<CommunityPost> {

    IPage<CommunityPost> listPosts(Integer topicType, Integer page, Integer size);

    CommunityPost getPostDetail(Long id);

    /**
     * 发布帖子
     * @param userId 发布用户ID
     * @param request 帖子发布请求
     * @return 新帖子的ID
     */
    Long createPost(Long userId, CommunityPostRequest request);

    boolean likePost(Long id);

    /**
     * 管理员删除帖子（软删除，设置status=0）
     */
    void deletePost(Long id);

    /**
     * 管理员列表查询（含已隐藏的帖子）
     */
    IPage<CommunityPost> adminListPosts(Integer topicType, Integer page, Integer size);
}

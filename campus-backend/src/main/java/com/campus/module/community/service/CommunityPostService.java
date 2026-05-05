package com.campus.module.community.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.community.dto.CommunityPostRequest;
import com.campus.module.community.entity.CommunityPost;

public interface CommunityPostService extends IService<CommunityPost> {

    IPage<CommunityPost> listPosts(Integer topicType, Integer page, Integer size);

    CommunityPost getPostDetail(Long id);

    Long createPost(Long userId, CommunityPostRequest request);

    boolean likePost(Long id);

    void deletePost(Long id);

    IPage<CommunityPost> adminListPosts(Integer topicType, Integer page, Integer size);
}

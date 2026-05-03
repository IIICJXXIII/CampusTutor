package com.campus.module.community.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.community.entity.CommunityPost;

public interface CommunityPostService extends IService<CommunityPost> {

    IPage<CommunityPost> listPosts(Integer topicType, Integer page, Integer size);

    CommunityPost getPostDetail(Long id);

    CommunityPost createPost(Long userId, CommunityPost post);

    boolean likePost(Long id);
}

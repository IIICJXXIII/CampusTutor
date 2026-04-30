package com.campus.module.community.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.community.entity.CommunityPost;

public interface CommunityPostService extends IService<CommunityPost> {

    Long createPost(Long userId, String title, String content, Integer topicType);

    IPage<CommunityPost> listPosts(Integer topicType, Integer page, Integer size);

    CommunityPost getPostDetail(Long postId);

    void likePost(Long userId, Long postId);
}

package com.campus.module.community.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.community.entity.CommunityReply;

public interface CommunityReplyService extends IService<CommunityReply> {

    Long createReply(Long userId, Long postId, String content);

    IPage<CommunityReply> listReplies(Long postId, Integer page, Integer size);
}

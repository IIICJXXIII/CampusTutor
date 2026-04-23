package com.campus.module.community.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.community.entity.CommunityReply;

public interface CommunityReplyService extends IService<CommunityReply> {

    IPage<CommunityReply> listReplies(Long postId, Integer page, Integer size);

    IPage<CommunityReply> listSubReplies(Long rootId, Long lastId, Integer size);

    CommunityReply createReply(Long userId, CommunityReply reply);

    void deleteReply(Long replyId);

    boolean likeReply(Long replyId);
}

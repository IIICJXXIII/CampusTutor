package com.campus.module.community.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.common.context.UserContext;
import com.campus.common.result.Result;
import com.campus.module.community.entity.CommunityPost;
import com.campus.module.community.entity.CommunityReply;
import com.campus.module.community.service.CommunityPostService;
import com.campus.module.community.service.CommunityReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "社区模块", description = "社区帖子、评论功能")
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityPostService postService;
    private final CommunityReplyService replyService;

    @Operation(summary = "发布帖子")
    @PostMapping("/posts")
    public Result<Long> createPost(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        Integer topicType = params.get("topicType") != null
                ? Integer.parseInt(params.get("topicType").toString())
                : 1;
        Long postId = postService.createPost(userId, title, content, topicType);
        return Result.success(postId);
    }

    @Operation(summary = "获取帖子列表")
    @GetMapping("/posts")
    public Result<IPage<CommunityPost>> listPosts(
            @RequestParam(required = false) Integer topicType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<CommunityPost> result = postService.listPosts(topicType, page, size);
        return Result.success(result);
    }

    @Operation(summary = "获取帖子详情")
    @GetMapping("/posts/{id}")
    public Result<CommunityPost> getPostDetail(@PathVariable Long id) {
        CommunityPost post = postService.getPostDetail(id);
        return Result.success(post);
    }

    @Operation(summary = "点赞帖子")
    @PostMapping("/posts/{id}/like")
    public Result<Void> likePost(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        postService.likePost(userId, id);
        return Result.success();
    }

    @Operation(summary = "发表评论")
    @PostMapping("/posts/{id}/replies")
    public Result<Long> createReply(@PathVariable Long id, @RequestBody Map<String, String> params) {
        Long userId = UserContext.getUserId();
        String content = params.get("content");
        Long replyId = replyService.createReply(userId, id, content);
        return Result.success(replyId);
    }

    @Operation(summary = "获取帖子评论列表")
    @GetMapping("/posts/{id}/replies")
    public Result<IPage<CommunityReply>> listReplies(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<CommunityReply> result = replyService.listReplies(id, page, size);
        return Result.success(result);
    }
}

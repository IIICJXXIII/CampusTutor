package com.campus.module.community.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.common.context.UserContext;
import com.campus.common.result.Result;
import com.campus.module.community.dto.CommunityPostRequest;
import com.campus.module.community.entity.CommunityNotification;
import com.campus.module.community.entity.CommunityPost;
import com.campus.module.community.entity.CommunityReply;
import com.campus.module.community.service.CommunityNotificationService;
import com.campus.module.community.service.CommunityPostService;
import com.campus.module.community.service.CommunityReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "社区")
@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityPostService postService;
    private final CommunityReplyService replyService;
    private final CommunityNotificationService notificationService;

    @Operation(summary = "获取帖子列表")
    @GetMapping("/posts")
    public Result<IPage<CommunityPost>> listPosts(
            @RequestParam(required = false) Integer topicType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(postService.listPosts(topicType, page, size));
    }

    @Operation(summary = "获取帖子详情")
    @GetMapping("/posts/{id}")
    public Result<CommunityPost> getPost(@PathVariable Long id) {
        return Result.success(postService.getPostDetail(id));
    }

    @Operation(summary = "发布帖子")
    @PostMapping("/posts")
    public Result<Long> createPost(@Valid @RequestBody CommunityPostRequest request) {
        Long userId = UserContext.getUserId();
        Long postId = postService.createPost(userId, request);
        return Result.success("发布成功", postId);
    }

    @Operation(summary = "点赞/取消点赞帖子")
    @PostMapping("/posts/{id}/like")
    public Result<Map<String, Object>> likePost(@PathVariable Long id) {
        boolean liked = postService.likePost(id);
        return Result.success(Map.of("liked", liked));
    }

    @Operation(summary = "获取帖子一级评论列表")
    @GetMapping("/posts/{postId}/replies")
    public Result<IPage<CommunityReply>> listReplies(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(replyService.listReplies(postId, page, size));
    }

    @Operation(summary = "获取子评论列表(游标分页)")
    @GetMapping("/replies/{rootId}/sub")
    public Result<IPage<CommunityReply>> listSubReplies(
            @PathVariable Long rootId,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "3") Integer size) {
        return Result.success(replyService.listSubReplies(rootId, lastId, size));
    }

    @Operation(summary = "评论/回复评论")
    @PostMapping("/posts/{postId}/replies")
    public Result<CommunityReply> createReply(
            @PathVariable Long postId,
            @RequestBody CommunityReply reply) {
        Long userId = UserContext.getUserId();
        reply.setPostId(postId);
        return Result.success(replyService.createReply(userId, reply));
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/replies/{replyId}")
    public Result<Void> deleteReply(@PathVariable Long replyId) {
        replyService.deleteReply(replyId);
        return Result.success(null);
    }

    @Operation(summary = "点赞/取消点赞评论")
    @PostMapping("/replies/{replyId}/like")
    public Result<Map<String, Object>> likeReply(@PathVariable Long replyId) {
        boolean liked = replyService.likeReply(replyId);
        return Result.success(Map.of("liked", liked));
    }

    @Operation(summary = "获取互动通知列表")
    @GetMapping("/notifications")
    public Result<IPage<CommunityNotification>> listNotifications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Long userId = UserContext.getUserId();
        return Result.success(notificationService.listNotifications(userId, page, size));
    }

    @Operation(summary = "获取未读通知数")
    @GetMapping("/notifications/unread-count")
    public Result<Integer> getUnreadNotificationCount() {
        Long userId = UserContext.getUserId();
        return Result.success(notificationService.getUnreadCount(userId));
    }

    @Operation(summary = "标记通知已读")
    @PutMapping("/notifications/{id}/read")
    public Result<Void> markNotificationRead(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    @Operation(summary = "全部标记已读")
    @PutMapping("/notifications/read-all")
    public Result<Void> markAllNotificationsRead() {
        Long userId = UserContext.getUserId();
        notificationService.markAllAsRead(userId);
        return Result.success();
    }
}

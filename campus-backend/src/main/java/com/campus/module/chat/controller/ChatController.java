package com.campus.module.chat.controller;

import com.campus.common.context.UserContext;
import com.campus.common.result.Result;
import com.campus.module.chat.dto.ChatMessageVO;
import com.campus.module.chat.dto.ChatSessionVO;
import com.campus.module.chat.service.ChatService;
import com.campus.module.user.entity.SysUser;
import com.campus.module.user.mapper.SysUserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天模块 REST API 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "聊天模块", description = "私聊相关接口")
public class ChatController {

    private final ChatService chatService;
    private final SysUserMapper sysUserMapper;

    @GetMapping("/sessions")
    @Operation(summary = "获取会话列表", description = "获取当前用户的所有聊天会话")
    public Result<List<ChatSessionVO>> getSessionList() {
        Long userId = UserContext.getUserId();
        List<ChatSessionVO> sessions = chatService.getSessionList(userId);
        return Result.success(sessions);
    }

    @GetMapping("/history/{targetUserId}")
    @Operation(summary = "获取聊天历史", description = "获取与指定用户的聊天记录")
    public Result<List<ChatMessageVO>> getChatHistory(
            @PathVariable @Parameter(description = "对方用户ID") Long targetUserId,
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") Integer page,
            @RequestParam(defaultValue = "50") @Parameter(description = "每页大小") Integer size) {

        Long userId = UserContext.getUserId();
        List<ChatMessageVO> messages = chatService.getChatHistory(userId, targetUserId, page, size);
        return Result.success(messages);
    }

    @PostMapping("/read/{targetUserId}")
    @Operation(summary = "标记消息已读", description = "将指定用户发来的消息标记为已读")
    public Result<Void> markAsRead(
            @PathVariable @Parameter(description = "对方用户ID") Long targetUserId) {

        Long userId = UserContext.getUserId();
        chatService.markAsRead(targetUserId, userId);
        return Result.success();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读消息数", description = "获取当前用户的未读消息总数")
    public Result<Integer> getUnreadCount() {
        Long userId = UserContext.getUserId();
        Integer count = chatService.getUnreadCount(userId);
        return Result.success(count);
    }

    @GetMapping("/user-info/{userId}")
    @Operation(summary = "获取聊天用户信息", description = "获取指定用户的基本信息用于聊天")
    public Result<Map<String, Object>> getChatUserInfo(
            @PathVariable @Parameter(description = "用户ID") Long userId) {

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatarUrl());
        userInfo.put("role", user.getRole());

        return Result.success(userInfo);
    }

    @PostMapping("/send")
    @Operation(summary = "发送消息", description = "发送聊天消息给指定用户")
    public Result<Long> sendMessage(@RequestBody Map<String, Object> message) {
        Long userId = UserContext.getUserId();
        Long targetUserId = Long.valueOf(message.get("targetUserId").toString());
        String content = (String) message.get("content");
        String type = (String) message.getOrDefault("type", "text");
        
        Long messageId = chatService.sendMessageWithTypeString(userId, targetUserId, content, type);
        return Result.success(messageId);
    }

    @GetMapping("/messages/{conversationId}")
    @Operation(summary = "获取会话消息", description = "获取指定会话的消息列表")
    public Result<List<ChatMessageVO>> getMessages(
            @PathVariable @Parameter(description = "会话ID") Long conversationId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer size) {
        Long userId = UserContext.getUserId();
        List<ChatMessageVO> messages = chatService.getMessagesByConversationId(conversationId, userId, page, size);
        return Result.success(messages);
    }
}

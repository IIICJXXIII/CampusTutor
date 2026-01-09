package com.campus.module.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.chat.dto.ChatSessionVO;
import com.campus.module.chat.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 聊天消息 Mapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 获取用户的会话列表
     * 查询所有与当前用户有过聊天记录的用户，并返回最新消息和未读数
     */
    @Select("""
                SELECT
                    t.target_id as target_user_id,
                    u.nickname as target_nickname,
                    u.avatar as target_avatar,
                    u.role as target_role,
                    m.content as last_message,
                    m.create_time as last_time,
                    (
                        SELECT COUNT(*)
                        FROM sys_chat_msg
                        WHERE sender_id = t.target_id
                        AND receiver_id = #{userId}
                        AND is_read = 0
                    ) as unread_count
                FROM (
                    SELECT
                        MAX(id) as max_id,
                        CASE
                            WHEN sender_id = #{userId} THEN receiver_id
                            ELSE sender_id
                        END as target_id
                    FROM sys_chat_msg
                    WHERE (sender_id = #{userId} OR receiver_id = #{userId})
                    AND sender_id != receiver_id
                    GROUP BY CASE
                        WHEN sender_id = #{userId} THEN receiver_id
                        ELSE sender_id
                    END
                ) t
                JOIN sys_chat_msg m ON m.id = t.max_id
                JOIN sys_user u ON u.id = t.target_id
                ORDER BY m.create_time DESC
            """)
    List<ChatSessionVO> findSessionsByUserId(@Param("userId") Long userId);

    /**
     * 获取用户所有未读消息数
     */
    @Select("SELECT COUNT(*) FROM sys_chat_msg WHERE receiver_id = #{receiverId} AND is_read = 0")
    Integer countUnreadByReceiverId(@Param("receiverId") Long receiverId);

    /**
     * 标记消息为已读
     */
    @Update("UPDATE sys_chat_msg SET is_read = 1 WHERE sender_id = #{senderId} AND receiver_id = #{receiverId} AND is_read = 0")
    int markAsRead(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}

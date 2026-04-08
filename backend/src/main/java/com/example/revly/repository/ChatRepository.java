package com.example.revly.repository;

import com.example.revly.model.Chat;
import com.example.revly.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Integer> {

    Optional<Chat> findById(int chatId);

    // Paginated list of user's chats, sorted newest first (for your notification panel)
    @Query("SELECT c FROM Chat c JOIN c.users u WHERE u = :user ORDER BY c.lastActivity DESC")
    Page<Chat> findChatsByUserOrderByLastActivityDesc(@Param("user") User user, Pageable pageable);

    // Update chat's last activity (called on every new message)
    @Modifying
    @Query("UPDATE Chat c SET c.lastActivity = :now WHERE c.chatId = :chatId")
    void updateLastActivity(@Param("chatId") int chatId, @Param("now") Timestamp now);

    // Increment unread count for a specific user in a chat (native because user_chat is join table)
    @Modifying
    @Query(value = "UPDATE user_chat SET unread_count = unread_count + 1 WHERE chat_id = :chatId AND user_id = :userId", nativeQuery = true)
    void incrementUnreadCount(@Param("chatId") int chatId, @Param("userId") int userId);

    // Get total unread count across ALL chats for a user (for live badge)
    @Query(value = "SELECT COALESCE(SUM(unread_count), 0) FROM user_chat WHERE user_id = :userId", nativeQuery = true)
    Integer getTotalUnreadCountForUser(@Param("userId") Integer userId);

    // Reset unread count for a specific chat (mark as read)
    @Modifying
    @Query(value = "UPDATE user_chat SET unread_count = 0 WHERE chat_id = :chatId AND user_id = :userId", nativeQuery = true)
    void markChatAsRead(@Param("chatId") int chatId, @Param("userId") int userId);
}
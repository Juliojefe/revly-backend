package com.example.revly.dto.response;

import com.example.revly.model.Chat;

public class ChatSummary {
    private Integer chatId;
    private String name;
    private int unreadCount;

    public ChatSummary() {}

    public ChatSummary(Chat chat) {
        this(chat, 0);
    }

    public ChatSummary(Chat chat, int unreadCount) {
        this.chatId = chat.getChatId();
        this.name = (chat.getName() != null && !chat.getName().trim().isEmpty())
                ? chat.getName().trim() : "Unnamed Chat";
        this.unreadCount = unreadCount;
    }

    // Getters and Setters
    public Integer getChatId() { return chatId; }
    public void setChatId(Integer chatId) { this.chatId = chatId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}
package com.example.revly.dto.response;

import com.example.revly.model.Chat;

public class ChatSummary {
    private Integer chatId;
    private String name;

    public ChatSummary() {}

    public ChatSummary(Chat chat) {
        this.chatId = chat.getChatId();
        this.name = (chat.getName() != null && !chat.getName().trim().isEmpty())
                ? chat.getName().trim()
                : "Unnamed Chat";
    }

    // Getters and Setters
    public Integer getChatId() { return chatId; }
    public void setChatId(Integer chatId) { this.chatId = chatId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
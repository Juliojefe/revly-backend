package com.example.revly.dto.response;

import com.example.revly.model.Chat;
import com.example.revly.model.User;

import java.util.ArrayList;
import java.util.List;

public class ChatSummary {
    private Integer chatId;
    private String name;
    private List<Participant> participants = new ArrayList<>();

    public ChatSummary() {}

    public ChatSummary(Chat chat) {
        this.chatId = chat.getChatId();
        this.name = chat.getName() != null ? chat.getName() : "Chat";

        for (User u : chat.getUsers()) {
            this.participants.add(new Participant(
                    u.getUserId(),
                    u.getName(),
                    u.getProfilePic()
            ));
        }
    }

    // Getters / Setters
    public Integer getChatId() { return chatId; }
    public void setChatId(Integer chatId) { this.chatId = chatId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Participant> getParticipants() { return participants; }
    public void setParticipants(List<Participant> participants) { this.participants = participants; }

    // inner class for each participant
    public static class Participant {
        private Integer userId;
        private String name;
        private String profilePic;

        public Participant(Integer userId, String name, String profilePic) {
            this.userId = userId;
            this.name = name;
            this.profilePic = profilePic;
        }

        // Getters
        public Integer getUserId() { return userId; }
        public String getName() { return name; }
        public String getProfilePic() { return profilePic; }
    }
}
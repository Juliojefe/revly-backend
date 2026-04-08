package com.example.revly.service;

import com.example.revly.dto.response.ChatSummary;
import com.example.revly.exception.ResourceNotFoundException;
import com.example.revly.exception.UnauthorizedException;
import com.example.revly.model.Chat;
import com.example.revly.model.User;
import com.example.revly.repository.ChatRepository;
import com.example.revly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private UserRepository userRepository;

    public ChatSummary getChatSummaryById(int chatId, User user) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ResourceNotFoundException("The chat you are looking for was not found"));
        if (!chat.getUsers().contains(user)) {
            throw new UnauthorizedException("That user is not a member of the chat");
        }
        return new ChatSummary(chat);
    }

    public ChatSummary createChat(String name, User currentUser, Set<Integer> userIds) {
        Chat chat = new Chat();
        chat.setName(name);
        chat.getUsers().add(currentUser);

        for (Integer id : userIds) {
            if (id != null && !id.equals(currentUser.getUserId())) {  // prevent duplicate current user
                Optional<User> userOptional = userRepository.findById(id);
                userOptional.ifPresent(chat.getUsers()::add);
            }
        }
        // chats must have 2 or more users
        if (chat.getUsers().size() < 2) {
            throw new IllegalArgumentException("A chat must have at least 2 users");
        }
        chatRepository.save(chat);
        return new ChatSummary(chat);
    }

    public Set<ChatSummary> getChatsForUser(User user) {
        return user.getChats().stream().map(ChatSummary::new).collect(Collectors.toSet());
    }
}
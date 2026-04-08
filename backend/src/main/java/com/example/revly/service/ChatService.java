package com.example.revly.service;

import com.example.revly.dto.response.ChatSummary;
import com.example.revly.exception.ResourceNotFoundException;
import com.example.revly.exception.UnauthorizedException;
import com.example.revly.model.Chat;
import com.example.revly.model.User;
import com.example.revly.repository.ChatRepository;
import com.example.revly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
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
            if (id != null && !id.equals(currentUser.getUserId())) {
                Optional<User> userOptional = userRepository.findById(id);
                userOptional.ifPresent(chat.getUsers()::add);
            }
        }

        if (chat.getUsers().size() < 2) {
            throw new IllegalArgumentException("A chat must have at least 2 users");
        }

        chatRepository.save(chat);
        return new ChatSummary(chat);
    }

    public Set<ChatSummary> getChatsForUser(User user) {
        return user.getChats().stream().map(ChatSummary::new).collect(Collectors.toSet());
    }

    // paginated list for your notification panel sorted by most recent message
    public Page<ChatSummary> getUserChatList(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return chatRepository.findChatsByUserOrderByLastActivityDesc(user, pageable)
                .map(chat -> new ChatSummary(chat));
    }

    // mark chat as read, called when user opens the chat
    public void markChatAsRead(int chatId, User user) {
        chatRepository.markChatAsRead(chatId, user.getUserId());
    }
}
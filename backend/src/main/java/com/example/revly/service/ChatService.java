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
import java.util.HashSet;
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
        if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("A chat must have at least 2 users");
        }
        Set<User> validUsers = new HashSet<>();
        validUsers.add(currentUser);
        for (Integer id : userIds) {
            if (id != null && !id.equals(currentUser.getUserId())) {
                Optional<User> userOptional = userRepository.findById(id);
                userOptional.ifPresent(validUsers::add);
            }
        }
        if (validUsers.size() < 2) {
            throw new IllegalArgumentException("A chat must have at least 2 valid users");
        }
        Chat chat = new Chat();
        chat.setName(name != null && !name.trim().isEmpty() ? name.trim() : null);
        chat.setLastActivity(new Timestamp(System.currentTimeMillis()));
        for (User u : validUsers) {
            chat.getUsers().add(u);
            u.getChats().add(chat);
        }
        Chat savedChat = chatRepository.save(chat);
        userRepository.save(currentUser);
        return new ChatSummary(savedChat);
    }

    public Set<ChatSummary> getChatsForUser(User user) {
        return user.getChats().stream().map(ChatSummary::new).collect(Collectors.toSet());
    }

    // paginated list for your sidebar with unread counts
    public Page<ChatSummary> getUserChatList(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Chat> chatsPage = chatRepository.findChatsByUserOrderByLastActivityDesc(user, pageable);
        return chatsPage.map(chat -> {
            int unread = chatRepository.getUnreadCountForChatAndUser(chat.getChatId(), user.getUserId());
            return new ChatSummary(chat, unread);
        });
    }

    // mark chat as read called when user opens the chat
    public void markChatAsRead(int chatId, User user) {
        chatRepository.markChatAsRead(chatId, user.getUserId());
    }
}
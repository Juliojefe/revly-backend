package com.example.revly.service;

import com.example.revly.dto.response.MessageDTO;
import com.example.revly.exception.ResourceNotFoundException;
import com.example.revly.exception.UnauthorizedException;
import org.springframework.transaction.annotation.Transactional;
import com.example.revly.model.Chat;
import com.example.revly.model.Message;
import com.example.revly.model.MessageImage;
import com.example.revly.model.User;
import com.example.revly.repository.ChatRepository;
import com.example.revly.repository.MessageRepository;
import com.example.revly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageImageService messageImageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public MessageDTO saveMessage(int chatId, String content, String email, List<String> imageUrls) {
        User sender = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ResourceNotFoundException("Chat was not found"));

        if (!chat.getUsers().contains(sender)) {
            throw new UnauthorizedException("You are not a member of this chat");
        }
        boolean hasText = content != null && !content.trim().isEmpty();
        boolean hasImages = imageUrls != null && !imageUrls.isEmpty();
        if (!hasText && !hasImages) {
            throw new IllegalArgumentException("Message cannot be empty. Must contain text or at least one image.");
        }
        if (imageUrls != null && imageUrls.size() > 3) {
            throw new IllegalArgumentException("Maximum 3 images allowed per message");
        }
        Message message = new Message();
        message.setContent(hasText ? content.trim() : null);  // store null if no text
        message.setUser(sender);
        message.setChat(chat);
        message.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        Message saved = messageRepository.save(message);
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String url : imageUrls) {
                messageImageService.addImage(saved.getMessageId(), url);
            }
        }
        //  notification logic
        Timestamp now = new Timestamp(System.currentTimeMillis());
        chatRepository.updateLastActivity(chatId, now);
        for (User member : chat.getUsers()) {
            if (!member.getUserId().equals(sender.getUserId())) {
                chatRepository.incrementUnreadCount(chatId, member.getUserId());
                pushUnreadCountToUser(member.getEmail());
            }
        }

        MessageDTO dto = mapToDTO(saved);
        messagingTemplate.convertAndSend("/topic/chat/" + chatId, dto);
        return dto;
    }

    public List<MessageDTO> getMessagesByChatId(int chatId, int page, int size, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ResourceNotFoundException("The chat you are looking for was not found"));
        if (!chat.getUsers().contains(user)) {
            throw new UnauthorizedException("You are not a member of the chat");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return messageRepository.findByChatChatId(chatId, pageable)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private MessageDTO mapToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setMessageId(message.getMessageId());
        dto.setContent(message.getContent());
        dto.setUserId(message.getUser().getUserId());
        dto.setSenderName(message.getUser().getName());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setImageUrls(messageImageService.getImagesByMessageId(message.getMessageId()).stream()
                .map(MessageImage::getImageUrl)
                .collect(Collectors.toList()));
        return dto;
    }

    private void pushUnreadCountToUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        int total = chatRepository.getTotalUnreadCountForUser(user.getUserId());
        messagingTemplate.convertAndSendToUser(email, "/queue/unread-count", total);
    }
}
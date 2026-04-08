package com.example.revly.service;

import com.example.revly.dto.response.MessageDTO;
import com.example.revly.exception.ResourceNotFoundException;
import com.example.revly.exception.UnauthorizedException;
import com.example.revly.model.Chat;
import com.example.revly.model.Message;
import com.example.revly.model.User;
import com.example.revly.model.MessageImage;
import com.example.revly.repository.ChatRepository;
import com.example.revly.repository.MessageRepository;
import com.example.revly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

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

    public MessageDTO saveMessage(int chatId, String content, String email, List<String> imageUrls) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ResourceNotFoundException("Chat was not found"));
        // only members of the chat can send messages
        if (!chat.getUsers().contains(user)) {
            throw new UnauthorizedException("You are not a member of this chat");
        }
        // A message must have at either text or image or both
        boolean hasText = content != null && !content.trim().isEmpty();
        boolean hasImages = imageUrls != null && !imageUrls.isEmpty();
        if (!hasText && !hasImages) {
            throw new IllegalArgumentException("Message must contain text, images, or both");
        }
        // max 3 images per message
        if (hasImages && imageUrls.size() > 3) {
            throw new IllegalArgumentException("Maximum 3 images allowed per message");
        }
        Message message = new Message();
        message.setContent(content);
        message.setUser(user);
        message.setChat(chat);
        Message saved = messageRepository.save(message);
        if (imageUrls != null && !imageUrls.isEmpty()) {
            for (String url : imageUrls) {
                messageImageService.addImage(saved.getMessageId(), url);
            }
        }
        return mapToDTO(saved);
    }

    public List<MessageDTO> getMessagesByChatId(int chatId, int page, int size, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ResourceNotFoundException("The chat you are looking for was not found"));

        // only members of the chat can read messages
        if (!chat.getUsers().contains(user)) {
            throw new UnauthorizedException("You are not a member of this chat");
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
        dto.setChatId(message.getChat().getChatId());
        dto.setCreatedAt(message.getCreatedAt());
        // Fetch images
        dto.setImageUrls(messageImageService.getImagesByMessageId(message.getMessageId()).stream()
                .map(MessageImage::getImageUrl)
                .collect(Collectors.toList()));
        return dto;
    }
}
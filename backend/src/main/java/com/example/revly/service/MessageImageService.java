package com.example.revly.service;

import com.example.revly.exception.ResourceNotFoundException;
import com.example.revly.model.Message;
import com.example.revly.model.MessageImage;
import com.example.revly.repository.MessageImageRepository;
import com.example.revly.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageImageService {

    @Autowired
    private MessageImageRepository messageImageRepository;

    @Autowired
    private MessageRepository messageRepository;

    public MessageImage addImage(int messageId, String imageUrl) {
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new ResourceNotFoundException("Message not found"));
        MessageImage image = new MessageImage();
        image.setImageUrl(imageUrl);
        image.setMessage(message);
        return messageImageRepository.save(image);
    }

    public List<MessageImage> getImagesByMessageId(int messageId) {
        return messageImageRepository.findByMessageMessageId(messageId);
    }
}
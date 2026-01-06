package com.example.revly.repository;
import com.example.revly.model.MessageImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageImageRepository extends JpaRepository<MessageImage, Integer> {
    List<MessageImage> findByMessageMessageId(Integer messageId);   //  get images from a message by the message's id
}
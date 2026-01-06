package com.example.revly.repository;
import com.example.revly.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByChatChatId(Integer chatId);
    List<Message> findByChatChatId(Integer chatId, Pageable pageable);  //  chat history by chatId
}
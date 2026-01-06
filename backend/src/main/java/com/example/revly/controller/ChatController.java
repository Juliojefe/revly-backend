package com.example.revly.controller;

import com.example.revly.dto.request.ChatCreateRequest;
import com.example.revly.dto.response.ChatSummary;
import com.example.revly.exception.ResourceNotFoundException;
import com.example.revly.exception.UnauthorizedException;
import com.example.revly.model.User;
import com.example.revly.repository.UserRepository;
import com.example.revly.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Set;

@RestController
@RequestMapping("api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return userRepository.findByEmail(principal.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatSummary> getChatSummaryById(@PathVariable("id") int chatId, Principal principal) {
        User currentUser = getCurrentUser(principal);
        ChatSummary summary = chatService.getChatSummaryById(chatId, currentUser);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/create")
    public ResponseEntity<ChatSummary> createChat(@RequestBody ChatCreateRequest request, Principal principal) {
        User currentUser = getCurrentUser(principal);
        ChatSummary summary = chatService.createChat(request.getName(), currentUser, request.getUserIds());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/user")
    public ResponseEntity<Set<ChatSummary>> getUserChats(Principal principal) {
        User currentUser = getCurrentUser(principal);
        Set<ChatSummary> chats = chatService.getChatsForUser(currentUser);
        return ResponseEntity.ok(chats);
    }
}

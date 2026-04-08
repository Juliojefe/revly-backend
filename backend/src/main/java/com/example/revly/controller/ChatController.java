package com.example.revly.controller;

import com.example.revly.dto.request.ChatCreateRequest;
import com.example.revly.dto.response.ChatSummary;
import com.example.revly.exception.ResourceNotFoundException;
import com.example.revly.exception.UnauthorizedException;
import com.example.revly.model.User;
import com.example.revly.repository.ChatRepository;
import com.example.revly.repository.UserRepository;
import com.example.revly.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    private User getCurrentUser(Principal principal) {
        if (principal == null) throw new UnauthorizedException("User not authenticated");
        return userRepository.findByEmail(principal.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChatSummary> getChatSummaryById(@PathVariable("id") int chatId, Principal principal) {
        User currentUser = getCurrentUser(principal);
        return ResponseEntity.ok(chatService.getChatSummaryById(chatId, currentUser));
    }

    @PostMapping("/create")
    public ResponseEntity<ChatSummary> createChat(@RequestBody ChatCreateRequest request, Principal principal) {
        User currentUser = getCurrentUser(principal);
        return ResponseEntity.ok(chatService.createChat(request.getName(), currentUser, request.getUserIds()));
    }

    @GetMapping("/user")
    public ResponseEntity<Page<ChatSummary>> getUserChats(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User currentUser = getCurrentUser(principal);
        Page<ChatSummary> chats = chatService.getUserChatList(currentUser, page, size);
        return ResponseEntity.ok(chats);
    }

    @PostMapping("/{chatId}/read")
    public ResponseEntity<Void> markChatAsRead(@PathVariable int chatId, Principal principal) {
        User currentUser = getCurrentUser(principal);
        chatService.markChatAsRead(chatId, currentUser);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(Principal principal) {
        User currentUser = getCurrentUser(principal);
        Integer count = chatRepository.getTotalUnreadCountForUser(currentUser.getUserId());
        return ResponseEntity.ok(count != null ? count : 0);
    }
}
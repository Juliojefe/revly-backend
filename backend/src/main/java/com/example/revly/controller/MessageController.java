package com.example.revly.controller;

import com.example.revly.dto.response.MessageDTO;
import com.example.revly.exception.UnauthorizedException;
import com.example.revly.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import com.example.revly.dto.request.MessageRequest;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;  // For broadcasting via WebSocket

    /**
     * Handles sending a message to a specific chat via HTTP.
     */
    @PostMapping("/{chatId}")
    public ResponseEntity<Void> sendMessage(@PathVariable int chatId, @RequestBody MessageRequest request, Principal principal) {
        principalCheck(principal);
        MessageDTO savedMessage = messageService.saveMessage(chatId, request.getContent(), principal.getName(), request.getImageUrls());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<List<MessageDTO>> getMessagesByChat(
            @PathVariable int chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal) {
        principalCheck(principal);
        // Updated to pass email so service can do membership check
        return ResponseEntity.ok(messageService.getMessagesByChatId(chatId, page, size, principal.getName()));
    }

    private void principalCheck(Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("User not authenticated");
        }
    }
}
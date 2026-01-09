package com.example.revly.controller;

import com.example.revly.dto.request.ImageRequest;
import com.example.revly.exception.UnauthorizedException;
import com.example.revly.model.MessageImage;
import com.example.revly.service.MessageImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/message-image")
public class MessageImageController {

    @Autowired
    private MessageImageService messageImageService;

    @PostMapping("/{messageId}")
    public ResponseEntity<MessageImage> addImageToMessage(@PathVariable int messageId, @RequestBody ImageRequest request, Principal principal) {
        principalCheck(principal);
        MessageImage image = messageImageService.addImage(messageId, request.getImageUrl());
        return ResponseEntity.ok(image);
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<List<MessageImage>> getImagesByMessage(@PathVariable int messageId, Principal principal) {
        principalCheck(principal);
        return ResponseEntity.ok(messageImageService.getImagesByMessageId(messageId));
    }

    private void principalCheck(Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("User not authenticated");
        }
    }
}
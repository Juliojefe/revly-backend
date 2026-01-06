package com.example.revly.controller;

import com.example.revly.dto.response.MutualFollowResponse;
import com.example.revly.exception.ResourceNotFoundException;
import com.example.revly.exception.UnauthorizedException;
import com.example.revly.model.User;
import com.example.revly.repository.UserRepository;
import com.example.revly.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("api/follow")
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return userRepository.findByEmail(principal.getName()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @GetMapping("/mutual/{userBId}")
    public ResponseEntity<MutualFollowResponse> checkMutualFollow(@PathVariable int userBId, Principal principal) {
        User user = getCurrentUser(principal);
        return ResponseEntity.ok(followService.checkMutualFollow(user.getUserId(), userBId));
    }

    @DeleteMapping("/{unfollowUserId}")
    public ResponseEntity<Void> unfollow(@PathVariable int unfollowUserId, Principal principal) {
        User user = getCurrentUser(principal);
        followService.unfollow(user.getUserId(), unfollowUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{followUserId}")
    public ResponseEntity<Boolean> follow(@PathVariable int followUserId, Principal principal) {
        User user = getCurrentUser(principal);
        followService.follow(user.getUserId(), followUserId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove-follower/{removeFollowerId}")
    public ResponseEntity<Boolean> removeFollower(@PathVariable int removeFollowerId, Principal principal) {
        User user = getCurrentUser(principal);
        followService.removeFollower(user.getUserId(), removeFollowerId);
        return ResponseEntity.ok().build();
    }
}
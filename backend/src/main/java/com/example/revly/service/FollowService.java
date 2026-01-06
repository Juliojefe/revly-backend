package com.example.revly.service;

import com.example.revly.dto.response.MutualFollowResponse;
import com.example.revly.exception.BadRequestException;
import com.example.revly.exception.ResourceNotFoundException;
import com.example.revly.model.User;
import com.example.revly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FollowService {

    @Autowired
    private UserRepository userRepository;

    public MutualFollowResponse checkMutualFollow(int userAId, int userBId) {
        User userA = findUserById(userAId);
        User userB = findUserById(userBId);
        return new MutualFollowResponse(userA, userB);
    }

    public void unfollow(int activeUserId, int unfollowUserId) {
        User active = findUserById(activeUserId);
        User unfollow = findUserById(unfollowUserId);
        if (!active.getFollowing().contains(unfollow)) {
            throw new BadRequestException("You are not following this user");
        }
        active.getFollowing().remove(unfollow);
        unfollow.getFollowers().remove(active);
        userRepository.save(active);
        userRepository.save(unfollow);
    }

    public void follow(int activeUserId, int followUserId) {
        User active = findUserById(activeUserId);
        User follow = findUserById(followUserId);
        if (active.getFollowing().contains(follow)) {
            throw new BadRequestException("You already follow this user");
        }
        active.getFollowing().add(follow);
        follow.getFollowers().add(active);
        userRepository.save(active);
        userRepository.save(follow);
    }

    public void removeFollower(int activeUserId, int removeFollowerId) {
        User active = findUserById(activeUserId);
        User removeFollow = findUserById(removeFollowerId);
        if (!active.getFollowers().contains(removeFollow)) {
            throw new BadRequestException("This user is not following you");
        }
        active.getFollowers().remove(removeFollow);
        removeFollow.getFollowing().remove(active);
        userRepository.save(active);
        userRepository.save(removeFollow);
    }

    // Helper — reuse & throw proper 404
    private User findUserById(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }
}
package com.example.revly.service;

import com.example.revly.dto.response.PostSummary;
import com.example.revly.model.Post;
import com.example.revly.model.User;
import com.example.revly.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ExploreService {

    @Autowired
    private PostRepository postRepository;

    /*
    Posts of those not followed by user, newest to oldest
     */
    public Page<PostSummary> getExplorePosts(Pageable pageable, User u) {
        Set<Integer> excludedUserIds = new HashSet<>();
        for (User followedUser : u.getFollowing()) {
            excludedUserIds.add(followedUser.getUserId());
        }
        excludedUserIds.add(u.getUserId()); // Add the current user to the excluded set
        Page<Post> posts = postRepository.findByUserUserIdNotIn(excludedUserIds, pageable);
        List<PostSummary> summaries = new ArrayList<>();
        for (Post post : posts.getContent()) {
            Boolean hasLiked = post.getLikers().contains(u);
            Boolean hasSaved = post.getSavers().contains(u);
            summaries.add(new PostSummary(post, hasLiked, hasSaved));
        }
        return new PageImpl<>(summaries, pageable, posts.getTotalElements());
    }

    public Page<PostSummary> getExplorePostsGuest(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        List<PostSummary> summaries = new ArrayList<>();
        for (Post post : posts.getContent()) {
            summaries.add(new PostSummary(post, false, false));
        }
        return new PageImpl<>(summaries, pageable, posts.getTotalElements());
    }
}
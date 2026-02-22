package com.example.revly.repository;
import com.example.revly.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleId(String googleId);

    @Query("SELECT followed.userId FROM User follower JOIN follower.following followed WHERE follower.userId = :userId AND followed.userId IN :authorIds")
    List<Integer> findFollowedAuthorIds(@Param("userId") Integer userId, @Param("authorIds") List<Integer> authorIds);
}
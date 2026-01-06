package com.example.revly.repository;
import com.example.revly.model.UserRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRolesRepository extends JpaRepository<UserRoles, Integer> {
    Optional<UserRoles> findByUserId(Integer userId);
}
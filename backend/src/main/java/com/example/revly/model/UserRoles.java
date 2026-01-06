package com.example.revly.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "user_roles")
public class UserRoles {
    @Id
    private Integer userId;

    @MapsId
    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @Column(name = "isadmin", nullable = false)
    private Boolean isAdmin = false;

    @Column(name = "ismechanic", nullable = false)
    private Boolean isMechanic = false;

    public UserRoles(Integer userId, User user, Boolean isAdmin, Boolean isMechanic) {
        this.userId = userId;
        this.user = user;
        this.isAdmin = isAdmin;
        this.isMechanic = isMechanic;
    }

    public UserRoles() {
        userId = null;
        user = null;
        isAdmin = false;
        isMechanic = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserRoles that = (UserRoles) o;

        return (userId != null && userId.equals(that.userId)) &&
                (isAdmin != null && isAdmin.equals(that.isAdmin)) &&
                (isMechanic != null && isMechanic.equals(that.isMechanic));
    }

    // Getters and setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public Boolean getIsMechanic() {
        return isMechanic;
    }

    public void setIsMechanic(Boolean isMechanic) {
        this.isMechanic = isMechanic;
    }
}
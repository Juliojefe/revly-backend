package com.example.revly.service;

import com.example.revly.dto.request.*;
import com.example.revly.dto.response.GetUserProfilePrivateResponse;
import com.example.revly.dto.response.GetUserProfilePublicResponse;
import com.example.revly.dto.response.UserNameAndPfp;
import com.example.revly.exception.BadRequestException;
import com.example.revly.exception.ConflictException;
import com.example.revly.exception.ForbiddenException;
import com.example.revly.exception.ResourceNotFoundException;
import com.example.revly.exception.UnauthorizedException;
import com.example.revly.model.User;
import com.example.revly.repository.UserRepository;
import com.example.revly.repository.UserRolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRolesRepository userRolesRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //  For admins only
    public Page<GetUserProfilePrivateResponse> getAllUsersPrivate(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        List<GetUserProfilePrivateResponse> responseList = new ArrayList<>();
        for (User u : userPage.getContent()) {
            responseList.add(new GetUserProfilePrivateResponse(u));
        }
        return new PageImpl<>(responseList, pageable, userPage.getTotalElements());
    }

    public GetUserProfilePrivateResponse getUserProfilePrivateById(int userId) {
        Optional<User> OptUser = userRepository.findById(userId);
        if (OptUser.isPresent()) {
            User u = OptUser.get();
            return new GetUserProfilePrivateResponse(u);
        } else {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    public UserNameAndPfp getUserNameAndPfpById(int userId){
        Optional<User> OptUser = userRepository.findById(userId);
        if (OptUser.isPresent()) {
            User u = OptUser.get();
            return new UserNameAndPfp(u);
        } else {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    public GetUserProfilePublicResponse getUserProfileById(int userId) {
        Optional<User> u = userRepository.findById(userId);
        if (u.isPresent()) {
            return new GetUserProfilePublicResponse(u.get());
        } else {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    public Page<Integer> getAllUserIds(Pageable pageable) {
        Page<User> allUsers = userRepository.findAll(pageable);
        List<Integer> ids = new ArrayList<>();
        for (User u : allUsers.getContent()) {
            ids.add(u.getUserId());
        }
        return new PageImpl<>(ids, pageable, allUsers.getTotalElements());
    }

    public void updateName(UpdateNameRequest request) {
        Optional<User> user = userRepository.findById(request.getUserId());
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + request.getUserId());
        }
        User tempUser = user.get();
        tempUser.setName(request.getName().trim());
        userRepository.save(tempUser);
    }

    public void updateEmail(UpdateEmailRequest request) {
        Optional<User> idUser = userRepository.findById(request.getUserId());
        if (idUser.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + request.getUserId());
        }
        Optional<User> emailUser = userRepository.findByEmail(request.getEmail().trim());
        if (emailUser.isPresent()) {
            User existingUser = emailUser.get();
            if (!Objects.equals(idUser.get().getUserId(), existingUser.getUserId())) {
                throw new ConflictException("Email already in use");
            }
        }
        User userToUpdate = idUser.get();
        userToUpdate.setEmail(request.getEmail().trim());
        userRepository.save(userToUpdate);
    }

    public void updatePassword(UpdatePasswordRequest request) {
        Optional<User> user = userRepository.findById(request.getUserId());
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + request.getUserId());
        }
        User tempUser = user.get();
        // Prevent password updates for Google-linked accounts
        if (tempUser.getPassword() == null && tempUser.getGoogleId() != null) {
            throw new ForbiddenException("Cannot update password for Google-linked accounts");
        }
        String oldPassword = request.getOldPassword().trim();
        String storedHashedPassword = tempUser.getPassword().trim();
        String newPassword = request.getNewPassword().trim();
        String salt = tempUser.getEmail().trim();
        String oldPassWordWithSalt = oldPassword + salt;
        if (!passwordEncoder.matches(oldPassWordWithSalt, storedHashedPassword)) {
            throw new UnauthorizedException("Incorrect old password");
        }
        if (!isValidPassword(newPassword)) {
            throw new BadRequestException("Invalid new password");
        }
        String newPasswordWithSalt = newPassword + salt;
        tempUser.setPassword(passwordEncoder.encode(newPasswordWithSalt));
        userRepository.save(tempUser);
    }

    public void makeAdmin(int requestUserId) {
        Optional<User> user = userRepository.findById(requestUserId);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + requestUserId);
        }
        User tempUser = user.get();
        tempUser.getUserRoles().setIsAdmin(true);
        userRepository.save(tempUser);
    }

    public void makeMechanic(int requestUserId) {
        Optional<User> user = userRepository.findById(requestUserId);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + requestUserId);
        }
        User tempUser = user.get();
        tempUser.getUserRoles().setIsMechanic(true);
        userRepository.save(tempUser);
    }

    public void makeRegularUser(int requestUserId) {
        Optional<User> user = userRepository.findById(requestUserId);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + requestUserId);
        }
        User tempUser = user.get();
        tempUser.getUserRoles().setIsMechanic(false);
        tempUser.getUserRoles().setIsAdmin(false);
        userRepository.save(tempUser);
    }

    public void updateProfilePic(UpdateProfilePicRequest request) {
        Optional<User> user = userRepository.findById(request.getUserId());
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + request.getUserId());
        }
        User tempUser = user.get();
        tempUser.setProfilePic(request.getPictureUrl());
        userRepository.save(tempUser);
    }


    public Boolean updateBio(UpdateBioRequest request) {
        Optional<User> user = userRepository.findById(request.getUserId());
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + request.getUserId());
        }
        User tempUser = user.get();
        tempUser.setProfilePic(request.getNewBio());
        userRepository.save(tempUser);
        return true;
    }

    public void deleteUser(int requestUserId) {
        Optional<User> user = userRepository.findById(requestUserId);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + requestUserId);
        }
        User tempUser = user.get();
        userRepository.delete(tempUser);
    }

    public boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_\\+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
        return password.matches(passwordRegex);
    }
}
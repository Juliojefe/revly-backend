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
import com.example.revly.model.Business;
import com.example.revly.model.User;
import com.example.revly.repository.BusinessRepository;
import com.example.revly.repository.UserRepository;
import com.example.revly.repository.UserRolesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRolesRepository userRolesRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private BusinessRepository businessRepository;

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

    public UserNameAndPfp getUserNameAndPfpById(int userId){
        Optional<User> OptUser = userRepository.findById(userId);
        if (OptUser.isPresent()) {
            User u = OptUser.get();
            return new UserNameAndPfp(u);
        } else {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
    }

    public Object getUserProfileById(int userId, Integer viewerUserId) {
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        boolean isOwner = viewerUserId != null && viewerUserId == userId;
        if (isOwner) {
            return new GetUserProfilePrivateResponse(target);
        }

        boolean viewerFollowsUser = viewerUserId != null && userRepository.isFollowingUser(viewerUserId, userId);
        return new GetUserProfilePublicResponse(target, viewerFollowsUser);
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

    public String uploadProfilePic(int userId, MultipartFile file) throws IOException {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        String url = fileUploadService.uploadFile(file);
        User tempUser = user.get();
        tempUser.setProfilePic(url);
        userRepository.save(tempUser);
        return url;
    }

    public Boolean updateBio(UpdateBioRequest request) {
        Optional<User> user = userRepository.findById(request.getUserId());
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + request.getUserId());
        }
        User tempUser = user.get();
        tempUser.setBiography(request.getNewBio());
        userRepository.save(tempUser);
        return true;
    }

    public Boolean updateBusinessLocation(int userId, UpdateBusinessLocationRequest request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        User user = userOpt.get();
        if (user.getUserRoles() == null || !Boolean.TRUE.equals(user.getUserRoles().getIsMechanic())) {
            throw new ForbiddenException("Only mechanics can have business locations");
        }
        String address = request.getAddress() != null ? request.getAddress().trim() : null;
        Double lat = request.getLat();
        Double lon = request.getLon();
        boolean isClearing = address == null || address.isEmpty();
        if (isClearing) {
            // Remove links from the owning side (Business)
            for (Business business : new HashSet<>(user.getBusinesses())) {
                business.getUsers().remove(user);
                businessRepository.save(business);
            }
            user.getBusinesses().clear();
            userRepository.save(user);
            return true;
        }
        if (lat == null || lon == null) {
            throw new BadRequestException("Business latitude and longitude are required when address is set");
        }
        // Reuse existing business or create new
        Optional<Business> existing = businessRepository.findByAddressAndLatAndLon(address, lat, lon);
        Business business = existing.orElseGet(() -> {
            Business newBusiness = new Business();
            newBusiness.setAddress(address);
            newBusiness.setLat(lat);
            newBusiness.setLon(lon);
            return businessRepository.save(newBusiness);
        });
        // 1. If user already has exactly this business do nothing
        if (user.getBusinesses().contains(business)) {
            return true;
        }
        // 2. Otherwise replace
        for (Business oldBusiness : new HashSet<>(user.getBusinesses())) {
            oldBusiness.getUsers().remove(user);
            businessRepository.save(oldBusiness);
        }
        user.getBusinesses().clear();
        // 3. Add the new one
        user.getBusinesses().add(business);
        business.getUsers().add(user);
        // Save the owning side so the join table is updated
        businessRepository.save(business);
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

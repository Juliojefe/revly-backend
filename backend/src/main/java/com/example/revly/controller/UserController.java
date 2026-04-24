package com.example.revly.controller;

import com.example.revly.dto.request.*;
import com.example.revly.dto.response.GetUserProfilePrivateResponse;
import com.example.revly.dto.response.UserNameAndPfp;
import com.example.revly.exception.ResourceNotFoundException;
import com.example.revly.exception.UnauthorizedException;
import com.example.revly.model.User;
import com.example.revly.repository.UserRepository;
import com.example.revly.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    //  private response containing all users (pageable)
    @GetMapping("/getAll")
    public ResponseEntity<Page<GetUserProfilePrivateResponse>> getAllUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUsersPrivate(PageRequest.of(page, size)));
    }

    //  public response containing only name and pfp
    @GetMapping("/{id}/name-and-pfp")
    public ResponseEntity<UserNameAndPfp> getUserNameAndPfpById(@PathVariable("id") int userId) {
        return ResponseEntity.ok(userService.getUserNameAndPfpById(userId));
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<?> getUserProfileById(@PathVariable("id") int userId, Principal principal) {
        User currentUser = getCurrentUserOrNull(principal);
        Integer viewerUserId = currentUser != null ? currentUser.getUserId() : null;
        return ResponseEntity.ok(userService.getUserProfileById(userId, viewerUserId));
    }

    @GetMapping("/all-ids")
    public ResponseEntity<Page<Integer>> getAllUserIds(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUserIds(PageRequest.of(page, size)));
    }

    @PatchMapping("/update-name/{id}/name")
    public ResponseEntity<Boolean> updateUserName(@RequestBody UpdateNameRequest request) {
        userService.updateName(request);
        return ResponseEntity.ok(true);
    }

    @PatchMapping("/update-email/{id}/email")
    public ResponseEntity<Boolean> updateEmail(@RequestBody UpdateEmailRequest request) {
        userService.updateEmail(request);
        return ResponseEntity.ok(true);
    }

    @PatchMapping("/update-password/{id}/password")
    public ResponseEntity<Boolean> updatePassword(@RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(request);
        return ResponseEntity.ok(true);
    }

    @PatchMapping("/make-admin/{id}")
    public ResponseEntity<Boolean> makeAdmin(@RequestBody int requestUserId) {
        userService.makeAdmin(requestUserId);
        return ResponseEntity.ok(true);
    }

    @PatchMapping("/make-mechanic/{id}")
    public ResponseEntity<Boolean> makeMechanic(@RequestBody int requestUserId) {
        userService.makeMechanic(requestUserId);
        return ResponseEntity.ok(true);
    }

    @PatchMapping("/make-regular-user/{id}")
    public ResponseEntity<Boolean> makeRegularUser(@RequestBody int requestUserId) {
        userService.makeRegularUser(requestUserId);
        return ResponseEntity.ok(true);
    }

    @PatchMapping("/update-profile-pic")
    public ResponseEntity<Boolean> updateProfilePic(@RequestBody UpdateProfilePicRequest request) {
        userService.updateProfilePic(request);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/update-profile-pic/upload")
    public ResponseEntity<String> uploadProfilePic(@RequestParam("userId") int userId,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(userService.uploadProfilePic(userId, file));
    }


    @PatchMapping("/update-bio")
    public ResponseEntity<Boolean> updateBio(@RequestBody UpdateBioRequest request) {
        return ResponseEntity.ok(userService.updateBio(request));
    }

    @PatchMapping("/business-location")
    public ResponseEntity<Boolean> updateBusinessLocation(@RequestBody UpdateBusinessLocationRequest request, Principal principal) {
        User user = getCurrentUser(principal);
        return ResponseEntity.ok(userService.updateBusinessLocation(user.getUserId(), request));
    }


    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<Boolean> deleteUser(@RequestBody int requestUserId) {
        userService.deleteUser(requestUserId);
        return ResponseEntity.ok(true);
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return getCurrentUserOrNull(principal);
    }

    private User getCurrentUserOrNull(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}

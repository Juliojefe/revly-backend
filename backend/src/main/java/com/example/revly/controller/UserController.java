package com.example.revly.controller;

import com.example.revly.dto.request.UpdateEmailRequest;
import com.example.revly.dto.request.UpdateNameRequest;
import com.example.revly.dto.request.UpdatePasswordRequest;
import com.example.revly.dto.request.UpdateProfilePicRequest;
import com.example.revly.dto.response.GetUserProfilePrivateResponse;
import com.example.revly.dto.response.GetUserProfilePublicResponse;
import com.example.revly.dto.response.UserNameAndPfp;
import com.example.revly.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/user")
public class UserController {

    @Autowired
    private UserService userService;

    //  private response containing all users (pageable)
    @GetMapping("/getAll")
    public ResponseEntity<Page<GetUserProfilePrivateResponse>> getAllUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getAllUsersPrivate(PageRequest.of(page, size)));
    }

    //  private verbose response containing all user details
    @GetMapping("/{id}/profile/private")
    public ResponseEntity<GetUserProfilePrivateResponse> getUserById(@PathVariable("id") int userId) {
        return ResponseEntity.ok(userService.getUserProfilePrivateById(userId));
    }

    //  public response containing only name and pfp
    @GetMapping("/{id}/name-and-pfp")
    public ResponseEntity<UserNameAndPfp> getUserNameAndPfpById(@PathVariable("id") int userId) {
        return ResponseEntity.ok(userService.getUserNameAndPfpById(userId));
    }

    //  public profile access
    @GetMapping("/{id}/profile/public")
    public ResponseEntity<GetUserProfilePublicResponse> getUserProfileById(@PathVariable("id") int userId) {
        return ResponseEntity.ok(userService.getUserProfileById(userId));
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

    @PatchMapping("/update-profile-pic/{id}/url-new-pic")
    public ResponseEntity<Boolean> updateProfilePic(@RequestBody UpdateProfilePicRequest request) {
        userService.updateProfilePic(request);
        return ResponseEntity.ok(true);
    }

    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<Boolean> deleteUser(@RequestBody int requestUserId) {
        userService.deleteUser(requestUserId);
        return ResponseEntity.ok(true);
    }
}
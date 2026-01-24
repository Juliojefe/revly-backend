package com.example.revly.service;

import com.example.revly.component.JwtTokenProvider;
import com.example.revly.dto.request.GoogleUserRegisterRequest;
import com.example.revly.dto.request.RefreshRequest;
import com.example.revly.dto.request.UserLoginRequest;
import com.example.revly.dto.request.UserRegisterRequest;
import com.example.revly.dto.response.AuthResponse;
import com.example.revly.dto.response.RefreshResponse;
import com.example.revly.exception.UnauthorizedException;
import com.example.revly.model.*;
import com.example.revly.model.RefreshToken;
import com.example.revly.model.User;
import com.example.revly.model.UserRoles;
import com.example.revly.repository.RefreshTokenRepository;
import com.example.revly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponse register(UserRegisterRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim() : "";
        String password = request.getPassword() != null ? request.getPassword().trim() : "";
        String confirmPassword = request.getConfirmPassword() != null ? request.getConfirmPassword().trim() : "";
        String name = request.getName() != null ? request.getName().trim() : "";
        String profilePic = request.getProfilePic() != null ? request.getProfilePic().trim() : getDefaultProfilePic();
        String biography = request.getBiography() != null ? request.getBiography().trim() : "";
        if (!password.equals(confirmPassword)) {
            return new AuthResponse("Passwords do not match");
        }
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return new AuthResponse("Email already in use");
        }
        if (!isValidPassword(password)) {
            return new AuthResponse("Invalid password:\n• At least 8 characters long\n• At least one uppercase letter\n• At least one lowercase letter\n• At least one number\n• At least one special character");
        }
        if (!email.contains("@") || email.length() < 4) {
            return new AuthResponse("Invalid email");
        }
        String[] nameParts = name.split("\\s+");
        if (nameParts.length != 2 || nameParts[0].length() < 2 || nameParts[1].length() < 2) {
            return new AuthResponse("Invalid first or last name");
        }
        if (biography.length() > 150) {
            return new AuthResponse("Bio should be shorter than 150 characters");
        }
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name);
        newUser.setProfilePic(profilePic);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setGoogleId(null);
        newUser.setBiography(biography);
        initializeUserCollections(newUser);
        userRepository.save(newUser);
        return createSuccessResponse(newUser, false);
    }

    public AuthResponse googleRegister(GoogleUserRegisterRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim() : "";
        String name = request.getName() != null ? request.getName().trim() : "";
        String googleId = request.getGoogleId() != null ? request.getGoogleId().trim() : "";
        String profilePic = request.getProfilePic() != null ? request.getProfilePic().trim() : getDefaultProfilePic();
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            return new AuthResponse("Email already in use");
        }
        Optional<User> existingGoogleUser = userRepository.findByGoogleId(googleId);
        if (existingGoogleUser.isPresent()) {
            return new AuthResponse("Google ID already registered");
        }
        if (!email.contains("@") || email.length() < 4) {
            return new AuthResponse("Invalid email");
        }
        String[] nameParts = name.split("\\s+");
        if (nameParts.length != 2 || nameParts[0].length() < 2 || nameParts[1].length() < 2) {
            return new AuthResponse("Invalid first or last name");
        }
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName(name);
        newUser.setProfilePic(profilePic);
        newUser.setPassword(null);
        newUser.setGoogleId(googleId);
        newUser.setBiography("");   //  empty bio for Google users initially
        initializeUserCollections(newUser);
        userRepository.save(newUser);
        return createSuccessResponse(newUser, true);
    }

    public AuthResponse login(UserLoginRequest loginRequest) {
        String email = loginRequest.getEmail() != null ? loginRequest.getEmail().trim() : "";
        String password = loginRequest.getPassword() != null ? loginRequest.getPassword().trim() : "";
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            return new AuthResponse("Invalid email or password");
        }
        User user = userOpt.get();
        return createSuccessResponse(user, false);
    }

    public AuthResponse googleLogin(String googleId) {
        // Ensures googleId is never null by replacing null with empty string, then trims whitespace
        googleId = googleId != null ? googleId.trim() : "";
        Optional<User> userOpt = userRepository.findByGoogleId(googleId);
        if (userOpt.isEmpty()) {
            return new AuthResponse("Google user not found, please register first");
        }
        User user = userOpt.get();
        return createSuccessResponse(user, true);
    }

    public RefreshResponse refreshToken(RefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshToken);
        if (tokenOpt.isEmpty()) {
            throw new UnauthorizedException("Token not valid");
        }
        RefreshToken tokenEntity = tokenOpt.get();
        if (tokenEntity.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.deleteByToken(refreshToken);
            throw new UnauthorizedException("Token not valid");
        }
        User user = tokenEntity.getUser();
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getUserId());
        return new RefreshResponse(newAccessToken);
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!@#$%^&*()_\\+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
        return password.matches(passwordRegex);
    }

    private String getDefaultProfilePic() {
        return "https://ui-avatars.com/api/?name=User&background=cccccc&color=222222&size=128";
    }

    private void initializeUserCollections(User user) {
        user.setChats(new HashSet<>());
        user.setFollowing(new HashSet<>());
        user.setFollowers(new HashSet<>());
        user.setSavedPosts(new HashSet<>());
        user.setLikedPosts(new HashSet<>());
        user.setOwnedPosts(new HashSet<>());
        UserRoles userRoles = new UserRoles();
        userRoles.setUser(user);
        userRoles.setIsAdmin(false);
        userRoles.setIsMechanic(false);
        user.setUserRoles(userRoles);
    }

    private AuthResponse createSuccessResponse(User user, boolean isGoogle) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getUserId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getUserId());
        RefreshToken refreshTokenEntity = new RefreshToken();
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenEntity.setUser(user);
        refreshTokenEntity.setExpiryDate(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshTokenRepository.save(refreshTokenEntity);
        UserRoles ur = user.getUserRoles();
        Boolean isAdmin = ur.getIsAdmin();
        Boolean isMechanic =  ur.getIsMechanic();
        return new AuthResponse(user.getName(), user.getUserId(), user.getEmail(), user.getProfilePic(), isGoogle, accessToken, refreshToken, isAdmin, isMechanic, user.getBiography());
    }
}
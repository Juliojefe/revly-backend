package com.example.revly.component;

import com.example.revly.exception.UnauthorizedException;
import com.example.revly.service.AuthService;
import com.example.revly.dto.request.GoogleUserRegisterRequest;
import com.example.revly.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private AuthService authService;

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        try {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oauthUser = oauthToken.getPrincipal();
            String googleId = oauthUser.getAttribute("sub");
            String email = oauthUser.getAttribute("email");
            String name = oauthUser.getAttribute("name");
            String profilePic = oauthUser.getAttribute("picture");
            if (email == null || name == null || googleId == null) {
                throw new IllegalArgumentException("Missing required attributes from Google: email, name, or sub");
            }
            String finalProfilePic = (profilePic != null) ? profilePic : "";

            // Try login first
            AuthResponse resp = authService.googleLogin(googleId);
            // If login fails (no token), auto-register
            if (resp.getAccessToken() == null) {
                GoogleUserRegisterRequest registerRequest =
                        new GoogleUserRegisterRequest(googleId, email, name, finalProfilePic);
                resp = authService.googleRegister(registerRequest);
            }
            buildRedirect(response, resp);
        } catch (Exception ex) {
            buildErrorRedirect(response, ex.getMessage());
        }
    }

    // Helper method to build the redirect URL for success or internal errors
    private void buildRedirect(HttpServletResponse response, AuthResponse authResponse) throws IOException {
        String baseUrl = "http://localhost:3000/auth-callback";
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl);

        if (authResponse != null && authResponse.getAccessToken() != null) {
            uriBuilder.queryParam("name", authResponse.getName());
            uriBuilder.queryParam("email", authResponse.getEmail());
            uriBuilder.queryParam("profilePic", authResponse.getProfilePic());
            uriBuilder.queryParam("isGoogle", authResponse.isGoogle());
            uriBuilder.queryParam("accessToken", authResponse.getAccessToken());
            uriBuilder.queryParam("refreshToken", authResponse.getRefreshToken());
            uriBuilder.queryParam("isAdmin", authResponse.getIsAdmin());
            uriBuilder.queryParam("isMechanic", authResponse.getIsMechanic());
        } else {
            String message = (authResponse != null && authResponse.getMessage() != null)
                    ? authResponse.getMessage() : "An unexpected error occurred";
            uriBuilder.queryParam("message", message);
        }

        response.sendRedirect(uriBuilder.toUriString());
    }

    // Helper method for error redirects (e.g., attribute missing or other exceptions)
    private void buildErrorRedirect(HttpServletResponse response, String errorMessage) throws IOException {
        String baseUrl = "http://localhost:3000/auth-callback";
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("error", errorMessage != null ? errorMessage : "An unexpected error occurred");
        response.sendRedirect(uriBuilder.toUriString());
    }
}
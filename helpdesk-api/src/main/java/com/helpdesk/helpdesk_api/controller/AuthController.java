package com.helpdesk.helpdesk_api.controller;

import com.helpdesk.helpdesk_api.dto.LoginRequest;
import com.helpdesk.helpdesk_api.dto.UserResponse;
import com.helpdesk.helpdesk_api.entity.User;
import com.helpdesk.helpdesk_api.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository contextRepository;
    private final UserRepository userRepository;

    public AuthController(
            AuthenticationManager authenticationManager,
            SecurityContextRepository contextRepository,
            UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.contextRepository = contextRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public UserResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(
                        request.username(), request.password()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // Persist authentication so it survives subsequent HTTP requests.
        contextRepository.saveContext(
                context, httpRequest, httpResponse);

        User user = userRepository.findByUsername(
                        authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "User not found"));

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole());
    }

    @GetMapping("/me")
    public UserResponse currentUser(Authentication authentication) {
        User user = userRepository.findByUsername(
                        authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole());
    }

    @PostMapping("/logout")
    public Map<String, String> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        return Map.of("message", "Logged out successfully");
    }
}


//This controller authenticates the supplied credentials, stores the authenticated security context in the HTTP session, and exposes a current-user endpoint.
//We explicitly save the SecurityContext because we are authenticating manually rather than using Spring Security's built-in form-login flow.
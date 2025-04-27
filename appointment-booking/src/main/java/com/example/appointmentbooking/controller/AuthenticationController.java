package com.example.appointmentbooking.controller;

import com.example.appointmentbooking.dto.AuthenticationResponse;
import com.example.appointmentbooking.dto.LoginUserDto;
import com.example.appointmentbooking.dto.RegisterUserDto;
import com.example.appointmentbooking.dto.VerifyUserDto;
import com.example.appointmentbooking.exception.UserNotVerifiedException;
import com.example.appointmentbooking.exception.VerificationCodeExpiredException;
import com.example.appointmentbooking.model.user.User;
import com.example.appointmentbooking.repository.UserRepository;
import com.example.appointmentbooking.service.authentication.AuthenticationService;
import com.example.appointmentbooking.utility.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto registerUserDto) {
        try {
            User registredUser = authenticationService.register(registerUserDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(registredUser);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginUserDto loginUserDto) {
        try {
            User authenticatedUser = authenticationService.authenticate(loginUserDto);
            String token = jwtUtil.generateToken(authenticatedUser);

            AuthenticationResponse response = new AuthenticationResponse();
            response.setUserId(authenticatedUser.getId());
            response.setUserRole(authenticatedUser.getUserRole());
            response.setToken(token);
            response.setExpiration(jwtUtil.getExpirationTime());
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);

        } catch (UserNotVerifiedException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyUserDto verifyUserDto) {
        try {
            authenticationService.verifyUser(verifyUserDto);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Account verified successfully");

        } catch (VerificationCodeExpiredException | IllegalArgumentException | UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resend(@RequestBody String email) {
        try {
            authenticationService.resendVerificationCode(email);
            return ResponseEntity.status(HttpStatus.OK).body("Verification code resend successfully");
        } catch (UserNotVerifiedException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

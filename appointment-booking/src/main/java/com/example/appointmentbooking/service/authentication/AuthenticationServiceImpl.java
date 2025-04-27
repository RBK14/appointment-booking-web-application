package com.example.appointmentbooking.service.authentication;

import com.example.appointmentbooking.dto.LoginUserDto;
import com.example.appointmentbooking.dto.RegisterUserDto;
import com.example.appointmentbooking.dto.VerifyUserDto;
import com.example.appointmentbooking.exception.UserNotVerifiedException;
import com.example.appointmentbooking.exception.VerificationCodeExpiredException;
import com.example.appointmentbooking.model.user.User;
import com.example.appointmentbooking.model.user.UserRole;
import com.example.appointmentbooking.repository.UserRepository;
import com.example.appointmentbooking.service.mail.MailService;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final MailService mailService;

    @PostConstruct
    public void createAdminAccount() {
        Optional<User> optionalAdmin = userRepository.findByUserRole(UserRole.ADMIN);

        if (optionalAdmin.isEmpty()) {
            User admin = new User();

            admin.setEmail("admin@test.com");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setName("Admin");
            admin.setPhone(null);
            admin.setUserRole(UserRole.ADMIN);
            admin.setEnabled(true);
            admin.setVerificationCode(null);
            admin.setVerificationCodeExpiration(null);
            userRepository.save(admin);
        } else {
            System.out.println("Admin account already exists");
        }
    }

    @Override
    public User register(RegisterUserDto input) {
        if (userRepository.findByEmail(input.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already in use");
        }

        User user = new User();

        user.setEmail(input.getEmail());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setName(input.getName());
        user.setPhone(input.getPhone());
        user.setUserRole(UserRole.CLIENT);
        user.setEnabled(false);
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(10));
        //sendVerificationMail(user);

        return userRepository.save(user);
    }

    @Override
    public User authenticate(LoginUserDto input) {
        User user = userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new UserNotVerifiedException("Account not verified. Please verify your account");
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    input.getEmail(),
                    input.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Incorrect email or password");
        }


        return user;
    }

    @Override
    public void verifyUser(VerifyUserDto input) {
        Optional<User> optionalUser = userRepository.findByEmail(input.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getVerificationCodeExpiration().isBefore(LocalDateTime.now())) {
                throw new VerificationCodeExpiredException("Verification code has expired");
            }
            if (user.getVerificationCode().equals(input.getVerificationCode())) {
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiration(null);
                userRepository.save(user);
            } else {
                throw new IllegalArgumentException("Invalid verification code");
            }
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    @Override
    public void resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.isEnabled()) {
                throw new IllegalStateException("User is already verified");
            }
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiration(LocalDateTime.now().plusMinutes(10));
            sendVerificationMail(user);
            userRepository.save(user);
        } else {
            throw new UsernameNotFoundException("User not found");
        }
    }

    @Override
    public void sendVerificationMail(User user) {
        String subject = "Verification Code";
        String verificationCode = user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            mailService.sendEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;

        return String.valueOf(code);
    }
}

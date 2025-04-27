package com.example.appointmentbooking.service.authentication;

import com.example.appointmentbooking.dto.LoginUserDto;
import com.example.appointmentbooking.dto.RegisterUserDto;
import com.example.appointmentbooking.dto.VerifyUserDto;
import com.example.appointmentbooking.model.user.User;

public interface AuthenticationService {

    User register(RegisterUserDto input);

    User authenticate(LoginUserDto input);

    void verifyUser(VerifyUserDto input);

    void resendVerificationCode(String email);

    void sendVerificationMail(User user);
}

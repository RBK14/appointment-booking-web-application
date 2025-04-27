package com.example.appointmentbooking.service;

import com.example.appointmentbooking.model.user.User;
import com.example.appointmentbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public List<User> allUsers() {
        return new ArrayList<>(userRepository.findAll());
    }
}

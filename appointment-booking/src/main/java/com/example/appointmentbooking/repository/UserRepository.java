package com.example.appointmentbooking.repository;

import com.example.appointmentbooking.model.user.User;
import com.example.appointmentbooking.model.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserRole(UserRole userRole);

    Optional<User> findByVerificationCode(String verificationCode);
}

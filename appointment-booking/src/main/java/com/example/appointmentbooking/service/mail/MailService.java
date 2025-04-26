package com.example.appointmentbooking.service.mail;


import jakarta.mail.MessagingException;

public interface MailService {

    void sendEmail(String to, String subject, String body) throws MessagingException;
}

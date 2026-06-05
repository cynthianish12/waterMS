package com.utilitybilling.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/** Sends OTP codes through the configured SMTP server. */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendOtp(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Utility Billing OTP");
        message.setText("Your OTP is " + code + ". It expires soon.");
        try {
            mailSender.send(message);
        } catch (Exception ignored) {
            // Local development may run without SMTP; the OTP remains stored for testing.
        }
    }

    public boolean sendBillApproved(String email, String body) {
        return send(email, "Utility Bill Approved", body);
    }

    public boolean sendBillRejected(String email, String body) {
        return send(email, "Utility Bill Rejected", body);
    }

    public boolean sendPaymentCompletedEmail(String email, String body) {
        return send(email, "Payment Completed Successfully", body);
    }

    public boolean sendPartialPaymentEmail(String email, String body) {
        return send(email, "Partial Payment Received", body);
    }

    private boolean send(String email, String subject, String body) {
        if (email == null || email.isBlank()) {
            log.warn("Email not sent because recipient is blank. subject={}", subject);
            return false;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);
        try {
            log.info("Sending email. to={}, subject={}", email, subject);
            mailSender.send(message);
            log.info("Email sent successfully. to={}, subject={}", email, subject);
            return true;
        } catch (Exception ex) {
            log.warn("Email sending failed. to={}, subject={}, error={}", email, subject, ex.getMessage());
            return false;
        }
    }
}

package nhantr.musicapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nhantr.musicapp.enums.ErrorCode;
import nhantr.musicapp.exception.AppException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:no-reply@musicflow.local}")
    private String fromEmail;

    public void sendRegistrationOtp(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("MusicFlow - Ma OTP xac thuc dang ky");
        message.setText("Ma OTP cua ban la: " + otp + "\nMa co hieu luc trong 5 phut.");

        try {
            mailSender.send(message);
            log.info("Registration OTP sent to {}", email);
        } catch (MailException ex) {
            log.error("Failed to send OTP email to {}", email, ex);
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR.getCode(), "Failed to send OTP email");
        }
    }

    public void sendLoginOtp(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("MusicFlow - Ma OTP xac thuc dang nhap");
        message.setText("Ma OTP dang nhap cua ban la: " + otp + "\nMa co hieu luc trong 5 phut.");

        try {
            mailSender.send(message);
            log.info("Login OTP sent to {}", email);
        } catch (MailException ex) {
            log.error("Failed to send login OTP email to {}", email, ex);
            throw new AppException(ErrorCode.EXTERNAL_SERVICE_ERROR.getCode(), "Failed to send OTP email");
        }
    }
}

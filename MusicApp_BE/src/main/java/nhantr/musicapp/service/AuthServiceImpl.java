package nhantr.musicapp.service;

import static java.time.LocalDateTime.now;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import nhantr.musicapp.dto.request.LoginRequest;
import nhantr.musicapp.dto.request.RegisterRequest;
import nhantr.musicapp.dto.request.SendOtpRequest;
import nhantr.musicapp.dto.response.LoginResponse;
import nhantr.musicapp.dto.response.RefreshTokenRespose;
import nhantr.musicapp.dto.request.UpdatePasswordRequest;
import nhantr.musicapp.dto.response.UserResponse;
import nhantr.musicapp.entity.User;
import nhantr.musicapp.enums.ErrorCode;
import nhantr.musicapp.enums.Role;
import nhantr.musicapp.enums.UserStatus;
import nhantr.musicapp.exception.AppException;
import nhantr.musicapp.mapper.UserMapper;
import nhantr.musicapp.repository.UserRepository;
import nhantr.musicapp.util.JwtUtil;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class AuthServiceImpl implements AuthService {

    final UserRepository userRepository;
    final PasswordEncoder passwordEncoder;
    final JwtUtil jwtUtil;
    final RedisService redisService;
    final UserMapper userMapper;
    final EmailService emailService;

    private static final String OTP_KEY_PREFIX = "auth:register:otp:";
    private static final String OTP_COOLDOWN_KEY_PREFIX = "auth:register:otp:cooldown:";
    private static final long OTP_TTL_SECONDS = 300L;
    private static final long OTP_COOLDOWN_SECONDS = 60L;
    private static final String OTP_KEY_PREFIX_LOGIN = "auth:login:otp:";
    private static final String OTP_COOLDOWN_KEY_PREFIX_LOGIN = "auth:login:otp:cooldown:";


    @Override
    public String sendRegisterOtp(SendOtpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS.getCode(), ErrorCode.EMAIL_ALREADY_EXISTS.getMessage());
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String cooldownKey = OTP_COOLDOWN_KEY_PREFIX + normalizedEmail;
        if (redisService.getValue(cooldownKey) != null) {
            throw new AppException(ErrorCode.OTP_RESEND_TOO_FAST.getCode(), ErrorCode.OTP_RESEND_TOO_FAST.getMessage());
        }

        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        redisService.setValue(OTP_KEY_PREFIX + normalizedEmail, otp, OTP_TTL_SECONDS);
        redisService.setValue(cooldownKey, "1", OTP_COOLDOWN_SECONDS);
        emailService.sendRegistrationOtp(normalizedEmail, otp);

        return "OTP has been sent to your email";
    }

    @Override
    public String sendLoginOtp(SendOtpRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        // Check user exists
        if (!userRepository.existsByEmail(normalizedEmail)) {
            throw new AppException(ErrorCode.USER_NOT_FOUND.getCode(), ErrorCode.USER_NOT_FOUND.getMessage());
        }

        String cooldownKey = OTP_COOLDOWN_KEY_PREFIX_LOGIN + normalizedEmail;
        if (redisService.getValue(cooldownKey) != null) {
            throw new AppException(ErrorCode.OTP_RESEND_TOO_FAST.getCode(), ErrorCode.OTP_RESEND_TOO_FAST.getMessage());
        }

        String otp = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1_000_000));
        redisService.setValue(OTP_KEY_PREFIX_LOGIN + normalizedEmail, otp, OTP_TTL_SECONDS);
        redisService.setValue(cooldownKey, "1", OTP_COOLDOWN_SECONDS);
        emailService.sendLoginOtp(normalizedEmail, otp);

        return "OTP has been sent to your email";
    }

    @Override
    public LoginResponse verifyLoginOtp(nhantr.musicapp.dto.request.VerifyLoginOtpRequest request) {
        if (request.getOtp() == null || request.getOtp().isBlank()) {
            throw new AppException(ErrorCode.OTP_REQUIRED.getCode(), ErrorCode.OTP_REQUIRED.getMessage());
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String otpKey = OTP_KEY_PREFIX_LOGIN + normalizedEmail;
        String cachedOtp = redisService.getValue(otpKey);
        if (cachedOtp == null) {
            throw new AppException(ErrorCode.OTP_EXPIRED.getCode(), ErrorCode.OTP_EXPIRED.getMessage());
        }
        if (!cachedOtp.equals(request.getOtp().trim())) {
            throw new AppException(ErrorCode.INVALID_OTP.getCode(), ErrorCode.INVALID_OTP.getMessage());
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND.getCode(), ErrorCode.USER_NOT_FOUND.getMessage()));

        // Clean up OTP
        redisService.deleteKey(otpKey);

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        log.info("User '{}' logged in via OTP successfully", user.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getRemainingValidityMs(accessToken) / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }


    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS.getCode(), ErrorCode.USERNAME_ALREADY_EXISTS.getMessage());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS.getCode(), ErrorCode.EMAIL_ALREADY_EXISTS.getMessage());
        }

        if (request.getOtp() == null || request.getOtp().isBlank()) {
            throw new AppException(ErrorCode.OTP_REQUIRED.getCode(), ErrorCode.OTP_REQUIRED.getMessage());
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String otpKey = OTP_KEY_PREFIX + normalizedEmail;
        String cachedOtp = redisService.getValue(otpKey);
        if (cachedOtp == null) {
            throw new AppException(ErrorCode.OTP_EXPIRED.getCode(), ErrorCode.OTP_EXPIRED.getMessage());
        }
        if (!cachedOtp.equals(request.getOtp().trim())) {
            throw new AppException(ErrorCode.INVALID_OTP.getCode(), ErrorCode.INVALID_OTP.getMessage());
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .createdAt(now())
                .build();

        redisService.deleteKey(otpKey);

        log.info("Register success");

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository
                .findByUsername(request.getUsername())
                .or(() -> userRepository.findByEmail(request.getUsername()))
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS.getCode(), ErrorCode.INVALID_CREDENTIALS.getMessage()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS.getCode(), ErrorCode.INVALID_CREDENTIALS.getMessage());
        }

        if (user.getStatus() == UserStatus.BANNED) {
            throw new AppException(ErrorCode.USER_BANNED.getCode(), ErrorCode.USER_BANNED.getMessage());
        }

        String accessToken = jwtUtil.generateAccessToken(user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

        log.info("User '{}' logged in successfully", user.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getRemainingValidityMs(accessToken) / 1000)
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException(ErrorCode.TOKEN_REQUIRED.getCode(), ErrorCode.TOKEN_REQUIRED.getMessage());
        }
        redisService.blacklistToken(refreshToken, jwtUtil.getRemainingValidityMs(refreshToken));
        log.info("Token blacklisted successfully");
    }

    @Override
    public RefreshTokenRespose refreshToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AppException(ErrorCode.TOKEN_REQUIRED.getCode(), ErrorCode.TOKEN_REQUIRED.getMessage());
        }
        if (redisService.isTokenBlacklisted(token)) {
            throw new AppException(ErrorCode.TOKEN_LOGGED_OUT.getCode(), ErrorCode.TOKEN_LOGGED_OUT.getMessage());
        }

        String username;
        try {
            username = jwtUtil.extractUsername(token);
        } catch (Exception ex) {
            throw new AppException(ErrorCode.INVALID_TOKEN.getCode(), ErrorCode.INVALID_TOKEN.getMessage());
        }

        Optional<User> optionalUser = userRepository.findByUsername(username);
        User user = optionalUser.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND.getCode(), ErrorCode.USER_NOT_FOUND.getMessage()));

        if (!jwtUtil.validateToken(token, username)) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED.getCode(), ErrorCode.TOKEN_EXPIRED.getMessage());
        }

        String newToken = jwtUtil.generateAccessToken(user.getUsername());

        log.info("Access token refreshed for user '{}'", user.getUsername());

        return RefreshTokenRespose.builder()
                .accessToken(newToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getRemainingValidityMs(newToken) / 1000)
                .build();
    }

    @Override
    public String updatePassword(UpdatePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new AppException(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
        }
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new AppException(ErrorCode.PASSWORD_MISMATCH.getCode(), ErrorCode.PASSWORD_MISMATCH.getMessage());
        }

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND.getCode(), ErrorCode.USER_NOT_FOUND.getMessage()));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("User '{}' updated password successfully", user.getUsername());

        return "Password updated successfully";
    }
    

    @Override
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new AppException(ErrorCode.UNAUTHORIZED.getCode(), ErrorCode.UNAUTHORIZED.getMessage());
        }

        User user = userRepository
                .findByUsername(authentication.getName())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND.getCode(), ErrorCode.USER_NOT_FOUND.getMessage()));
        
        log.info("Current user '{}' retrieved successfully", user.getUsername());

        return userMapper.toResponse(user);
    }
}

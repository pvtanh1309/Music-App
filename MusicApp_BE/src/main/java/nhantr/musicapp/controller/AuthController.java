package nhantr.musicapp.controller;

import jakarta.validation.Valid;
import nhantr.musicapp.dto.request.LoginRequest;
import nhantr.musicapp.dto.request.RegisterRequest;
import nhantr.musicapp.dto.request.SendOtpRequest;
import nhantr.musicapp.dto.request.UpdatePasswordRequest;
import nhantr.musicapp.dto.response.APIResponse;
import nhantr.musicapp.dto.response.LoginResponse;
import nhantr.musicapp.dto.response.RefreshTokenRespose;
import nhantr.musicapp.dto.response.UserResponse;
import nhantr.musicapp.exception.AppException;
import nhantr.musicapp.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/send-otp")
    public ResponseEntity<APIResponse<String>> sendRegisterOtp(@Valid @RequestBody SendOtpRequest request) {
        return ResponseEntity.ok(APIResponse.success(authService.sendRegisterOtp(request)));
    }

    @PostMapping("/register")
    public ResponseEntity<APIResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(APIResponse.success(authService.register(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(APIResponse.success(authService.login(request)));
    }

    @PostMapping("/login/send-otp")
    public ResponseEntity<APIResponse<String>> sendLoginOtp(@Valid @RequestBody SendOtpRequest request) {
        return ResponseEntity.ok(APIResponse.success(authService.sendLoginOtp(request)));
    }

    @PostMapping("/login/verify-otp")
    public ResponseEntity<APIResponse<LoginResponse>> verifyLoginOtp(@Valid @RequestBody nhantr.musicapp.dto.request.VerifyLoginOtpRequest request) {
        return ResponseEntity.ok(APIResponse.success(authService.verifyLoginOtp(request)));
    }

    @PostMapping("/logout")
    public ResponseEntity<APIResponse<String>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = extractBearerToken(authHeader);
        authService.logout(token);
        return ResponseEntity.ok(APIResponse.success("Logged out successfully"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<APIResponse<RefreshTokenRespose>> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String token = extractBearerToken(authHeader);
        return ResponseEntity.ok(APIResponse.success(authService.refreshToken(token)));
    }

    @GetMapping("/me")
    public ResponseEntity<APIResponse<UserResponse>> me() {
        return ResponseEntity.ok(APIResponse.success(authService.getCurrentUser()));
    }

    @PutMapping("/update-password")
    public ResponseEntity<APIResponse<String>> updatePassword(@Valid @RequestBody  UpdatePasswordRequest request) {
        return ResponseEntity.ok(APIResponse.success(authService.updatePassword(request)));
    }

    private String extractBearerToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(400, "Authorization header must start with Bearer");
        }
        return authHeader.substring(7);
    }
}

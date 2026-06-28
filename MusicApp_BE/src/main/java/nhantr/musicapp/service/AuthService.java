package nhantr.musicapp.service;

import nhantr.musicapp.dto.request.LoginRequest;
import nhantr.musicapp.dto.request.RegisterRequest;
import nhantr.musicapp.dto.request.SendOtpRequest;
import nhantr.musicapp.dto.response.LoginResponse;
import nhantr.musicapp.dto.response.UserResponse;
import nhantr.musicapp.dto.response.RefreshTokenRespose;
import nhantr.musicapp.dto.request.UpdatePasswordRequest;

public interface AuthService {

    String sendRegisterOtp(SendOtpRequest request);

    String sendLoginOtp(SendOtpRequest request);

    LoginResponse verifyLoginOtp(nhantr.musicapp.dto.request.VerifyLoginOtpRequest request);

    UserResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    void logout(String token);

    RefreshTokenRespose refreshToken(String token);

    String updatePassword(UpdatePasswordRequest request);

    UserResponse getCurrentUser();
}

package nhantr.musicapp.service;

import nhantr.musicapp.dto.request.*;
import nhantr.musicapp.dto.response.LoginResponse;
import nhantr.musicapp.dto.response.RefreshTokenRespose;
import nhantr.musicapp.dto.response.UserResponse;
import nhantr.musicapp.entity.User;
import nhantr.musicapp.enums.ErrorCode;
import nhantr.musicapp.enums.Role;
import nhantr.musicapp.enums.UserStatus;
import nhantr.musicapp.exception.AppException;
import nhantr.musicapp.mapper.UserMapper;
import nhantr.musicapp.repository.UserRepository;
import nhantr.musicapp.util.JwtUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests cho AuthServiceImpl.
 *
 * Chiến lược:
 *  - Không dùng Spring Context -> chạy nhanh, không cần DB/Redis thật.
 *  - Mockito mock toàn bộ dependency (UserRepository, RedisService, EmailService, JwtUtil, ...).
 *  - Mỗi test theo mô hình AAA: Arrange → Act → Assert.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    // ─── Mocks ───────────────────────────────────────────────────────────────
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private RedisService redisService;
    @Mock private UserMapper userMapper;
    @Mock private EmailService emailService;

    // ─── System Under Test ───────────────────────────────────────────────────
    @InjectMocks
    private AuthServiceImpl authService;

    // ─── Dữ liệu dùng chung ──────────────────────────────────────────────────
    private static final String TEST_EMAIL       = "test@gmail.com";
    private static final String TEST_USERNAME    = "testuser";
    private static final String TEST_PASSWORD    = "password123";
    private static final String TEST_OTP         = "123456";
    private static final String FAKE_ACCESS_TOKEN  = "fake.access.token";
    private static final String FAKE_REFRESH_TOKEN = "fake.refresh.token";

    private User mockUser;
    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(UUID.randomUUID())
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .password("encodedPassword")
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        mockUserResponse = UserResponse.builder()
                .id(mockUser.getId())
                .username(TEST_USERNAME)
                .email(TEST_EMAIL)
                .role(Role.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }

    // =========================================================================
    // 1. sendRegisterOtp
    // =========================================================================
    @Nested
    @DisplayName("1. sendRegisterOtp()")
    class SendRegisterOtpTests {

        private SendOtpRequest request;

        @BeforeEach
        void setUp() {
            request = SendOtpRequest.builder().email(TEST_EMAIL).build();
        }

        @Test
        @DisplayName("✅ Thành công: Email chưa tồn tại và chưa bị cooldown")
        void sendRegisterOtp_Success() {
            // Arrange
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(redisService.getValue(contains("cooldown"))).thenReturn(null);

            // Act
            String result = authService.sendRegisterOtp(request);

            // Assert
            assertThat(result).isEqualTo("OTP has been sent to your email");
            // setValue phải được gọi đúng 2 lần: 1 cho OTP, 1 cho cooldown
            verify(redisService, times(2)).setValue(anyString(), anyString(), anyLong());
            verify(emailService, times(1)).sendRegistrationOtp(eq(TEST_EMAIL), anyString());
        }

        @Test
        @DisplayName("❌ Thất bại: Email đã tồn tại trong hệ thống")
        void sendRegisterOtp_Fail_EmailAlreadyExists() {
            // Arrange
            when(userRepository.existsByEmail(anyString())).thenReturn(true);

            // Act & Assert
            AppException ex = catchThrowableOfType(
                    () -> authService.sendRegisterOtp(request), AppException.class);

            assertThat(ex.getCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS.getCode());
            assertThat(ex.getMessage()).isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS.getMessage());

            // Đảm bảo không gửi email
            verify(emailService, never()).sendRegistrationOtp(anyString(), anyString());
            verifyNoInteractions(redisService);
        }

        @Test
        @DisplayName("❌ Thất bại: Gửi lại OTP quá nhanh (trong cooldown 60s)")
        void sendRegisterOtp_Fail_OtpCooldownActive() {
            // Arrange
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(redisService.getValue(contains("cooldown"))).thenReturn("1"); // cooldown đang active

            // Act & Assert
            AppException ex = catchThrowableOfType(
                    () -> authService.sendRegisterOtp(request), AppException.class);

            assertThat(ex.getCode()).isEqualTo(ErrorCode.OTP_RESEND_TOO_FAST.getCode());
            assertThat(ex.getMessage()).isEqualTo(ErrorCode.OTP_RESEND_TOO_FAST.getMessage());
            verify(emailService, never()).sendRegistrationOtp(anyString(), anyString());
        }

        @Test
        @DisplayName("✅ Email được normalize: chữ hoa → chữ thường, bỏ khoảng trắng")
        void sendRegisterOtp_Success_EmailNormalized() {
            // Arrange: email có chữ hoa + khoảng trắng thừa
            request = SendOtpRequest.builder().email("  TEST@Gmail.COM  ").build();
            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(redisService.getValue(anyString())).thenReturn(null);

            // Act
            authService.sendRegisterOtp(request);

            // Assert: email gửi cho emailService phải đã được normalize
            verify(emailService).sendRegistrationOtp(eq("test@gmail.com"), anyString());
        }
    }

    // =========================================================================
    // 2. sendLoginOtp
    // =========================================================================
    @Nested
    @DisplayName("2. sendLoginOtp()")
    class SendLoginOtpTests {

        private SendOtpRequest request;

        @BeforeEach
        void setUp() {
            request = SendOtpRequest.builder().email(TEST_EMAIL).build();
        }

        @Test
        @DisplayName("✅ Thành công: User tồn tại và chưa bị cooldown")
        void sendLoginOtp_Success() {
            // Arrange
            when(userRepository.existsByEmail(anyString())).thenReturn(true);
            when(redisService.getValue(contains("cooldown"))).thenReturn(null);

            // Act
            String result = authService.sendLoginOtp(request);

            // Assert
            assertThat(result).isEqualTo("OTP has been sent to your email");
            verify(redisService, times(2)).setValue(anyString(), anyString(), anyLong());
            verify(emailService, times(1)).sendLoginOtp(eq(TEST_EMAIL), anyString());
        }

        @Test
        @DisplayName("❌ Thất bại: Tài khoản không tồn tại (USER_NOT_FOUND)")
        void sendLoginOtp_Fail_UserNotFound() {
            // Arrange
            when(userRepository.existsByEmail(anyString())).thenReturn(false);

            // Act & Assert
            AppException ex = catchThrowableOfType(
                    () -> authService.sendLoginOtp(request), AppException.class);

            assertThat(ex.getCode()).isEqualTo(ErrorCode.USER_NOT_FOUND.getCode());
            verifyNoInteractions(redisService, emailService);
        }

        @Test
        @DisplayName("❌ Thất bại: Gửi lại OTP quá nhanh (trong cooldown 60s)")
        void sendLoginOtp_Fail_OtpCooldownActive() {
            // Arrange
            when(userRepository.existsByEmail(anyString())).thenReturn(true);
            when(redisService.getValue(contains("cooldown"))).thenReturn("1");

            // Act & Assert
            AppException ex = catchThrowableOfType(
                    () -> authService.sendLoginOtp(request), AppException.class);

            assertThat(ex.getCode()).isEqualTo(ErrorCode.OTP_RESEND_TOO_FAST.getCode());
            verify(emailService, never()).sendLoginOtp(anyString(), anyString());
        }
    }

    // =========================================================================
    // 3. register
    // =========================================================================
    @Nested
    @DisplayName("3. register()")
    class RegisterTests {

        private RegisterRequest request;

        @BeforeEach
        void setUp() {
            request = RegisterRequest.builder()
                    .username(TEST_USERNAME)
                    .email(TEST_EMAIL)
                    .password(TEST_PASSWORD)
                    .otp(TEST_OTP)
                    .build();
        }

        @Test
        @DisplayName("✅ Thành công: Đăng ký user mới với OTP hợp lệ")
        void register_Success() {
            // Arrange
            when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
            when(redisService.getValue(anyString())).thenReturn(TEST_OTP); // OTP khớp
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(mockUser);
            when(userMapper.toResponse(any(User.class))).thenReturn(mockUserResponse);

            // Act
            UserResponse result = authService.register(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo(TEST_USERNAME);
            assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
            verify(userRepository).save(any(User.class));
            verify(redisService).deleteKey(anyString()); // OTP phải được xóa sau đăng ký
        }

        @Test
        @DisplayName("❌ Thất bại: Username đã tồn tại")
        void register_Fail_UsernameAlreadyExists() {
            // Arrange
            when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

            // Act & Assert
            AppException ex = catchThrowableOfType(
                    () -> authService.register(request), AppException.class);

            assertThat(ex.getCode()).isEqualTo(ErrorCode.USERNAME_ALREADY_EXISTS.getCode());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ Thất bại: Email đã tồn tại")
        void register_Fail_EmailAlreadyExists() {
            // Arrange
            when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

            // Act & Assert
            AppException ex = catchThrowableOfType(
                    () -> authService.register(request), AppException.class);

            assertThat(ex.getCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS.getCode());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ Thất bại: OTP đã hết hạn (không còn trong Redis)")
        void register_Fail_OtpExpired() {
            // Arrange
            when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
            when(redisService.getValue(anyString())).thenReturn(null); // OTP đã hết hạn

            // Act & Assert
            AppException ex = catchThrowableOfType(
                    () -> authService.register(request), AppException.class);

            assertThat(ex.getCode()).isEqualTo(ErrorCode.OTP_EXPIRED.getCode());
        }

        @Test
        @DisplayName("❌ Thất bại: OTP sai")
        void register_Fail_InvalidOtp() {
            // Arrange
            when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
            when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
            when(redisService.getValue(anyString())).thenReturn("999999"); // OTP trong Redis khác OTP gửi lên

            // Act & Assert
            AppException ex = catchThrowableOfType(
                    () -> authService.register(request), AppException.class);

            assertThat(ex.getCode()).isEqualTo(ErrorCode.INVALID_OTP.getCode());
        }
    }

    // =========================================================================
    // 4. login (Username/Password)
    // =========================================================================
    @Nested
    @DisplayName("4. login()")
    class LoginTests {

        private LoginRequest request;

        @BeforeEach
        void setUp() {
            request = LoginRequest.builder()
                    .username(TEST_USERNAME)
                    .password(TEST_PASSWORD)
                    .build();
        }

        @Test
        @DisplayName("✅ Thành công: Đăng nhập bằng username + password đúng")
        void login_Success() {
            // Arrange
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
            when(passwordEncoder.matches(TEST_PASSWORD, mockUser.getPassword())).thenReturn(true);
            when(jwtUtil.generateAccessToken(TEST_USERNAME)).thenReturn(FAKE_ACCESS_TOKEN);
            when(jwtUtil.generateRefreshToken(TEST_USERNAME)).thenReturn(FAKE_REFRESH_TOKEN);
            when(jwtUtil.getRemainingValidityMs(FAKE_ACCESS_TOKEN)).thenReturn(3600000L);
            when(userMapper.toResponse(mockUser)).thenReturn(mockUserResponse);

            // Act
            LoginResponse result = authService.login(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo(FAKE_ACCESS_TOKEN);
            assertThat(result.getRefreshToken()).isEqualTo(FAKE_REFRESH_TOKEN);
            assertThat(result.getTokenType()).isEqualTo("Bearer");
            assertThat(result.getUser().getUsername()).isEqualTo(TEST_USERNAME);
        }

        @Test
        @DisplayName("✅ Thành công: Đăng nhập bằng email thay vì username")
        void login_Success_WithEmail() {
            // Arrange: request dùng email thay vì username
            request = LoginRequest.builder().username(TEST_EMAIL).password(TEST_PASSWORD).build();

            when(userRepository.findByUsername(TEST_EMAIL)).thenReturn(Optional.empty()); // không tìm thấy theo username
            when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockUser)); // tìm theo email
            when(passwordEncoder.matches(TEST_PASSWORD, mockUser.getPassword())).thenReturn(true);
            when(jwtUtil.generateAccessToken(TEST_USERNAME)).thenReturn(FAKE_ACCESS_TOKEN);
            when(jwtUtil.generateRefreshToken(TEST_USERNAME)).thenReturn(FAKE_REFRESH_TOKEN);
            when(jwtUtil.getRemainingValidityMs(FAKE_ACCESS_TOKEN)).thenReturn(3600000L);
            when(userMapper.toResponse(mockUser)).thenReturn(mockUserResponse);

            // Act
            LoginResponse result = authService.login(request);

            // Assert
            assertThat(result.getAccessToken()).isNotNull();
        }

        @Test
        @DisplayName("❌ Thất bại: Tài khoản không tồn tại")
        void login_Fail_UserNotFound() {
            // Arrange
            when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            AppException ex = catchThrowableOfType(
                    () -> authService.login(request), AppException.class);

            assertThat(ex.getCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS.getCode());
        }

        @Test
        @DisplayName("❌ Thất bại: Sai mật khẩu")
        void login_Fail_WrongPassword() {
            // Arrange
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false); // sai mật khẩu

            // Act & Assert
            AppException ex = catchThrowableOfType(
                    () -> authService.login(request), AppException.class);

            assertThat(ex.getCode()).isEqualTo(ErrorCode.INVALID_CREDENTIALS.getCode());
        }

        @Test
        @DisplayName("❌ Thất bại: Tài khoản bị BANNED")
        void login_Fail_UserBanned() {
            // Arrange: user đang bị ban
            User bannedUser = User.builder()
                    .username(TEST_USERNAME)
                    .email(TEST_EMAIL)
                    .password("encodedPassword")
                    .role(Role.USER)
                    .status(UserStatus.BANNED) // <-- bị ban
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(bannedUser));
            when(passwordEncoder.matches(TEST_PASSWORD, bannedUser.getPassword())).thenReturn(true);

            // Act & Assert
            AppException ex = catchThrowableOfType(
                    () -> authService.login(request), AppException.class);

            assertThat(ex.getCode()).isEqualTo(ErrorCode.USER_BANNED.getCode());
        }
    }

    // =========================================================================
    // 5. logout
    // =========================================================================
    @Nested
    @DisplayName("5. logout()")
    class LogoutTests {

        @Test
        @DisplayName("✅ Thành công: Đăng xuất với refresh token hợp lệ")
        void logout_Success() {
            // Arrange
            when(jwtUtil.getRemainingValidityMs(FAKE_REFRESH_TOKEN)).thenReturn(7200000L);

            // Act
            authService.logout(FAKE_REFRESH_TOKEN);

            // Assert: token phải được thêm vào blacklist
            verify(redisService, times(1)).blacklistToken(FAKE_REFRESH_TOKEN, 7200000L);
        }

        @Test
        @DisplayName("❌ Thất bại: Token là null")
        void logout_Fail_NullToken() {
            // Act & Assert
            AppException ex = catchThrowableOfType(
                    () -> authService.logout(null), AppException.class);

            assertThat(ex.getCode()).isEqualTo(ErrorCode.TOKEN_REQUIRED.getCode());
            verifyNoInteractions(redisService);
        }

        @Test
        @DisplayName("❌ Thất bại: Token là chuỗi rỗng")
        void logout_Fail_BlankToken() {
            // Act & Assert
            AppException ex = catchThrowableOfType(
                    () -> authService.logout("   "), AppException.class);

            assertThat(ex.getCode()).isEqualTo(ErrorCode.TOKEN_REQUIRED.getCode());
        }
    }

    // =========================================================================
    // 6. refreshToken
    // =========================================================================
    @Nested
    @DisplayName("6. refreshToken()")
    class RefreshTokenTests {

        @Test
        @DisplayName("✅ Thành công: Làm mới access token với refresh token hợp lệ")
        void refreshToken_Success() {
            // Arrange
            when(redisService.isTokenBlacklisted(FAKE_REFRESH_TOKEN)).thenReturn(false);
            when(jwtUtil.extractUsername(FAKE_REFRESH_TOKEN)).thenReturn(TEST_USERNAME);
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
            when(jwtUtil.validateToken(FAKE_REFRESH_TOKEN, TEST_USERNAME)).thenReturn(true);
            when(jwtUtil.generateAccessToken(TEST_USERNAME)).thenReturn(FAKE_ACCESS_TOKEN);
            when(jwtUtil.getRemainingValidityMs(FAKE_ACCESS_TOKEN)).thenReturn(3600000L);

            // Act
            RefreshTokenRespose result = authService.refreshToken(FAKE_REFRESH_TOKEN);

            // Assert
            assertThat(result.getAccessToken()).isEqualTo(FAKE_ACCESS_TOKEN);
            assertThat(result.getTokenType()).isEqualTo("Bearer");
            assertThat(result.getExpiresIn()).isEqualTo(3600L);
        }

        @Test
        @DisplayName("❌ Thất bại: Token là null")
        void refreshToken_Fail_NullToken() {
            AppException ex = catchThrowableOfType(
                    () -> authService.refreshToken(null), AppException.class);
            assertThat(ex.getCode()).isEqualTo(ErrorCode.TOKEN_REQUIRED.getCode());
        }

        @Test
        @DisplayName("❌ Thất bại: Token đã bị blacklist (đã logout)")
        void refreshToken_Fail_TokenBlacklisted() {
            // Arrange
            when(redisService.isTokenBlacklisted(FAKE_REFRESH_TOKEN)).thenReturn(true);

            // Act & Assert
            AppException ex = catchThrowableOfType(
                    () -> authService.refreshToken(FAKE_REFRESH_TOKEN), AppException.class);

            assertThat(ex.getCode()).isEqualTo(ErrorCode.TOKEN_LOGGED_OUT.getCode());
        }

        @Test
        @DisplayName("❌ Thất bại: Token không hợp lệ (không parse được)")
        void refreshToken_Fail_InvalidToken() {
            // Arrange
            when(redisService.isTokenBlacklisted(FAKE_REFRESH_TOKEN)).thenReturn(false);
            when(jwtUtil.extractUsername(FAKE_REFRESH_TOKEN)).thenThrow(new RuntimeException("bad token"));

            // Act & Assert
            AppException ex = catchThrowableOfType(
                    () -> authService.refreshToken(FAKE_REFRESH_TOKEN), AppException.class);

            assertThat(ex.getCode()).isEqualTo(ErrorCode.INVALID_TOKEN.getCode());
        }

        @Test
        @DisplayName("❌ Thất bại: Token đã hết hạn (expired)")
        void refreshToken_Fail_TokenExpired() {
            // Arrange
            when(redisService.isTokenBlacklisted(FAKE_REFRESH_TOKEN)).thenReturn(false);
            when(jwtUtil.extractUsername(FAKE_REFRESH_TOKEN)).thenReturn(TEST_USERNAME);
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
            when(jwtUtil.validateToken(FAKE_REFRESH_TOKEN, TEST_USERNAME)).thenReturn(false); // expired

            // Act & Assert
            AppException ex = catchThrowableOfType(
                    () -> authService.refreshToken(FAKE_REFRESH_TOKEN), AppException.class);

            assertThat(ex.getCode()).isEqualTo(ErrorCode.TOKEN_EXPIRED.getCode());
        }
    }

    // =========================================================================
    // 7. updatePassword
    // =========================================================================
    @Nested
    @DisplayName("7. updatePassword()")
    class UpdatePasswordTests {

        private UpdatePasswordRequest request;
        private Authentication mockAuthentication;
        private SecurityContext mockSecurityContext;

        @BeforeEach
        void setUp() {
            request = UpdatePasswordRequest.builder()
                    .newPassword("newPass123")
                    .confirmNewPassword("newPass123")
                    .build();

            mockAuthentication = mock(Authentication.class);
            mockSecurityContext = mock(SecurityContext.class);

            when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
            SecurityContextHolder.setContext(mockSecurityContext);
        }

        @AfterEach
        void tearDown() {
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("✅ Thành công: Đổi mật khẩu khi xác thực hợp lệ và 2 mật khẩu khớp nhau")
        void updatePassword_Success() {
            // Arrange
            when(mockAuthentication.isAuthenticated()).thenReturn(true);
            when(mockAuthentication.getName()).thenReturn(TEST_USERNAME);
            when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
            when(passwordEncoder.encode("newPass123")).thenReturn("encodedNewPass");

            // Act
            String result = authService.updatePassword(request);

            // Assert
            assertThat(result).isEqualTo("Password updated successfully");
            assertThat(mockUser.getPassword()).isEqualTo("encodedNewPass");
            verify(userRepository).save(mockUser);
        }

        @Test
        @DisplayName("❌ Thất bại: Người dùng chưa đăng nhập (anonymousUser)")
        void updatePassword_Fail_NotAuthenticated() {
            // Arrange: mô phỏng user anonymous
            when(mockAuthentication.isAuthenticated()).thenReturn(true);
            when(mockAuthentication.getName()).thenReturn("anonymousUser");

            // Act & Assert
            AppException ex = catchThrowableOfType(
                    () -> authService.updatePassword(request), AppException.class);

            assertThat(ex.getCode()).isEqualTo(ErrorCode.UNAUTHORIZED.getCode());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("❌ Thất bại: Mật khẩu mới và xác nhận mật khẩu không khớp")
        void updatePassword_Fail_PasswordMismatch() {
            // Arrange
            UpdatePasswordRequest mismatchRequest = UpdatePasswordRequest.builder()
                    .newPassword("newPass123")
                    .confirmNewPassword("different456") // không khớp
                    .build();

            when(mockAuthentication.isAuthenticated()).thenReturn(true);
            when(mockAuthentication.getName()).thenReturn(TEST_USERNAME);

            // Act & Assert
            AppException ex = catchThrowableOfType(
                    () -> authService.updatePassword(mismatchRequest), AppException.class);

            assertThat(ex.getCode()).isEqualTo(ErrorCode.PASSWORD_MISMATCH.getCode());
            verify(userRepository, never()).save(any());
        }
    }
}

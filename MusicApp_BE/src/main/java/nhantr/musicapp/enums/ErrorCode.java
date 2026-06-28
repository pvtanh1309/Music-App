package nhantr.musicapp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ErrorCode enum containing all error codes and messages used in the application
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 400 - Bad Request / Client Validation Errors
    INVALID_REQUEST(400, "Invalid request"),
    VALIDATION_FAILED(400, "Validation failed"),
    USERNAME_ALREADY_EXISTS(400, "Username already exists"),
    EMAIL_ALREADY_EXISTS(400, "Email already exists"),
    INVALID_USERNAME_FORMAT(400, "Username must be between 3 and 50 characters"),
    INVALID_EMAIL_FORMAT(400, "Invalid email format"),
    INVALID_PASSWORD_FORMAT(400, "Password must be at least 6 characters"),
    TOKEN_REQUIRED(400, "Token is required"),
    EMPTY_REQUEST_BODY(400, "Request body cannot be empty"),
    INVALID_INPUT_DATA(400, "Invalid input data provided"),
    PASSWORD_MISMATCH(400, "New password and confirm new password do not match"),
    INVALID_CURRENT_PASSWORD(400, "Current password is incorrect"),
    OTP_REQUIRED(400, "OTP is required"),
    INVALID_OTP(400, "OTP is invalid"),
    OTP_EXPIRED(400, "OTP has expired"),
    OTP_RESEND_TOO_FAST(429, "Please wait before requesting another OTP"),

    // 401 - Unauthorized / Authentication Errors
    UNAUTHORIZED(401, "Unauthorized"),
    INVALID_CREDENTIALS(401, "Invalid username or password"),
    USER_BANNED(403, "User account is banned"),
    TOKEN_EXPIRED(401, "Token has expired"),
    INVALID_TOKEN(401, "Invalid token"),
    TOKEN_MALFORMED(401, "Token is malformed or corrupted"),
    TOKEN_LOGGED_OUT(401, "Token has been logged out"),
    AUTHENTICATION_FAILED(401, "Authentication failed"),
    INSUFFICIENT_PERMISSIONS(401, "Insufficient permissions to perform this action"),
    SESSION_EXPIRED(401, "Session has expired"),

    // 403 - Forbidden
    FORBIDDEN(403, "Forbidden"),
    ACCESS_DENIED(403, "Access denied"),
    RESOURCE_FORBIDDEN(403, "You do not have permission to access this resource"),

    // 404 - Not Found
    USER_NOT_FOUND(404, "User not found"),
    RESOURCE_NOT_FOUND(404, "Resource not found"),
    PLAYLIST_NOT_FOUND(404, "Playlist not found"),
    SONG_NOT_FOUND(404, "Song not found"),
    ARTIST_NOT_FOUND(404, "Artist not found"),

    // 409 - Conflict
    DUPLICATE_ENTRY(409, "Duplicate entry"),
    RESOURCE_ALREADY_EXISTS(409, "Resource already exists"),
    USER_ALREADY_FOLLOWS(409, "User already follows this resource"),

    // 429 - Too Many Requests
    RATE_LIMIT_EXCEEDED(429, "Rate limit exceeded. Please try again later"),
    TOO_MANY_LOGIN_ATTEMPTS(429, "Too many login attempts. Please try again later"),

    // 500 - Internal Server Error
    INTERNAL_SERVER_ERROR(500, "Internal server error"),
    DATABASE_ERROR(500, "Database error occurred"),
    REDIS_ERROR(500, "Redis error occurred"),
    FILE_PROCESSING_ERROR(500, "Error processing file"),
    EXTERNAL_SERVICE_ERROR(500, "External service error"),
    ENCRYPTION_ERROR(500, "Encryption error"),
    UNKNOWN_ERROR(500, "An unknown error occurred");

    private final int code;
    private final String message;

    /**
     * Get the error code value
     */
    public int getCode() {
        return code;
    }

    /**
     * Get the error message
     */
    public String getMessage() {
        return message;
    }
}

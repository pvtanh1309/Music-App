package nhantr.musicapp.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final long expirationMs;
    private final long refreshExpirationMs;



    public JwtUtil(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration-ms}") long expirationMs,
            @Value("${jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(String username) {
        return generateToken(username, Duration.ofMillis(expirationMs));
    }

    public String generateToken(String username, Duration duration) {
        Instant now = Instant.now();
        Instant expiry = now.plus(duration);
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }
    public String generateRefreshToken(String username) {
        return generateToken(username, Duration.ofMillis(refreshExpirationMs));
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.getSubject());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, claims -> claims.getExpiration());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean validateToken(String token, String username) {
        String extractedUsername = extractUsername(token);
        return extractedUsername.equals(username) && !isTokenExpired(token);
    }

    public long getRemainingValidityMs(String token) {
        long remaining = extractExpiration(token).getTime() - System.currentTimeMillis();
        return Math.max(remaining, 0L);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid JWT token", ex);
        }
    }
}

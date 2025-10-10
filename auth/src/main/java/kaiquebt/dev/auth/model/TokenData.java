package kaiquebt.dev.auth.model;

import java.time.Duration;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class TokenData {
    private static final Duration DEFAULT_TOKEN_EXPIRATION = Duration.ofMinutes(10);
    private static final Duration DEFAULT_TOKEN_COOLDOWN = Duration.ofMinutes(10);

    @Column(name = "token")
    private String token;
    
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;
    
    @Column(name = "last_token_created_at")
    private LocalDateTime lastTokenCreatedAt;

    public boolean isExpired() {
        return this.tokenExpiresAt == null || this.tokenExpiresAt.isBefore(LocalDateTime.now());
    }

    public LocalDateTime canResendTokenAfter() {
        if (this.lastTokenCreatedAt == null) {
            return LocalDateTime.now();
        }
        return this.lastTokenCreatedAt.plus(DEFAULT_TOKEN_COOLDOWN);
    }

    public boolean confirmToken(String token) {
        if (isTokenValid(token)) {
            markAsConfirmed();
            return true;
        }
        return false;
    }

    public boolean isTokenValid(String token) {
        if (this.token == null || this.tokenExpiresAt == null) {
            return false;
        }
        if (this.tokenExpiresAt.isBefore(LocalDateTime.now())) {
            return false;
        }
        return this.token.equals(token);
    }

    public void attachNewToken(String newToken) {
        this.token = newToken;
        this.tokenExpiresAt = LocalDateTime.now().plus(DEFAULT_TOKEN_EXPIRATION);
        this.lastTokenCreatedAt = LocalDateTime.now();
    }

    public void markAsConfirmed() {
        this.token = null;
        this.tokenExpiresAt = null;
    }


    public void clear() {
        this.token = null;
        this.tokenExpiresAt = null;
    }
    
    public void markAsExpired() {
        this.token = null;
        this.tokenExpiresAt = null;
    }
}

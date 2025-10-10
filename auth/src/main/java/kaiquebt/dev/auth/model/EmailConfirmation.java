package kaiquebt.dev.auth.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class EmailConfirmation extends TokenData {
    public EmailConfirmation(String token, LocalDateTime tokenExpiresAt,
            LocalDateTime lastTokenCreatedAt, Boolean confirmed,
            LocalDateTime confirmedAt) {
        super(token, tokenExpiresAt, lastTokenCreatedAt);
        this.confirmed = confirmed;
        this.confirmedAt = confirmedAt;
    }

    @Column(name = "email_confirmed", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean confirmed = false;

    @Column(name = "email_confirmed_at")
    private LocalDateTime confirmedAt;

    @Override
    public void markAsConfirmed() {
        super.markAsConfirmed();
        this.confirmed = true;
        this.confirmedAt = LocalDateTime.now();
    }

}
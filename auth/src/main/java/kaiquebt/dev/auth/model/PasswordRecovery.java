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
public class PasswordRecovery extends TokenData {
    public PasswordRecovery(String token, LocalDateTime tokenExpiresAt,
            LocalDateTime lastTokenCreatedAt, LocalDateTime recoveredAt,
            Integer tries) {
        super(token, tokenExpiresAt, lastTokenCreatedAt);
        this.recoveredAt = recoveredAt;
        this.tries = tries;
    }
    private static final int MAX_RECOVER_TRIES = 5;

    @Column(name = "password_recover_last")
    private LocalDateTime recoveredAt;

    @Column(name = "password_recover_tries", nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer tries;

    public boolean incrementTries() {
        if (this.tries == null) {
            this.tries = 0;
        }
        this.tries += 1;
        if (this.tries > MAX_RECOVER_TRIES) {
            return false;
        }
        return true;
    }

}
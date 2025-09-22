package kaiquebt.dev.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Set;

@MappedSuperclass
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BaseUser {
    public static enum RoleType {
        ROLE_USER,
        ROLE_AFFILIATE,
        ROLE_ADMIN,
        ROLE_BANNED;
        
        @Override
        public String toString() {
            return name();
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_roles", 
        joinColumns = @JoinColumn(name = "user_id"),
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "role"})
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<RoleType> roles;

    // APENAS CAMPOS PARA MAGIC LINK
    @Column(name = "email_confirmed", nullable = false)
    private Boolean emailConfirmed;
    
    @Column(name = "email_confirmed_at")
    private LocalDateTime emailConfirmedAt;
    
    @Column(name = "email_confirmation_token")
    private String emailConfirmationToken;
    
    @Column(name = "token_expires_at")
    private LocalDateTime tokenExpiresAt;


    // PASSWORD RECOVERY FIELDS
    @Column(name = "password_recover_token")
    private String passwordRecoverToken;

    @Column(name = "password_recover_expiration")
    private LocalDateTime passwordRecoverExpiration;

    @Column(name = "password_recover_tries", nullable = false)
    private Integer passwordRecoverTries;

    @Column(name = "password_recover_last")
    private LocalDateTime passwordRecoverLast;


    public static final Duration DEFAULT_TOKEN_EXPIRATION = Duration.ofHours(24);
    public static final Duration DEFAULT_USER_RECOVER_TOKEN_EXPIRATION = Duration.ofMinutes(30);


    public long canResendAfter() {
        if (tokenExpiresAt == null) {
            return 0;
        }
        
        LocalDateTime tokenGeneratedAt = tokenExpiresAt.minus(DEFAULT_TOKEN_EXPIRATION);
        LocalDateTime fiveMinutesAfterGeneration = tokenGeneratedAt.plusMinutes(5);
        
        if (LocalDateTime.now().isAfter(fiveMinutesAfterGeneration)) {
            return 0;
        }
        
        return fiveMinutesAfterGeneration.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public void clearPasswordRecoveryData() {
        this.passwordRecoverToken = null;
        this.passwordRecoverExpiration = null;
        this.passwordRecoverTries = 0;
        this.passwordRecoverLast = LocalDateTime.now();
    }

    public long canRecoverAfter() {        
        if (passwordRecoverLast != null) {
            System.out.println(this.passwordRecoverLast.plusMinutes(30).isAfter(LocalDateTime.now()));
            if (this.passwordRecoverLast.plusMinutes(30).isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("Houve uma alteração de senha recente, aguarde 30 minutos até a próxima recuperação");
            }
        }

        if (passwordRecoverExpiration == null) {
            return 0;
        }
        
        LocalDateTime tokenGeneratedAt = passwordRecoverExpiration.minus(DEFAULT_USER_RECOVER_TOKEN_EXPIRATION);
        LocalDateTime fiveMinutesAfterGeneration = tokenGeneratedAt.plusMinutes(5);
        
        if (LocalDateTime.now().isAfter(fiveMinutesAfterGeneration)) {
            return 0;
        }
        
        return fiveMinutesAfterGeneration.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

}
package kaiquebt.dev.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
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

    @Builder.Default
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "token", column = @Column(name = "email_confirmation_token")),
        @AttributeOverride(name = "tokenExpiresAt", column = @Column(name = "email_confirmation_token_expires_at")),
        @AttributeOverride(name = "lastTokenCreatedAt", column = @Column(name = "last_email_confirmation_token_created_at"))
    })
    private EmailConfirmation emailConfirmation = new EmailConfirmation();
    
    @Builder.Default
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "token", column = @Column(name = "password_recover_token")),
        @AttributeOverride(name = "tokenExpiresAt", column = @Column(name = "password_recover_token_expires_at")),
        @AttributeOverride(name = "lastTokenCreatedAt", column = @Column(name = "last_password_recover_token_created_at"))
    })
    private PasswordRecovery passwordRecovery = new PasswordRecovery(); 
}
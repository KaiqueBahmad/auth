package kaiquebt.dev.auth.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kaiquebt.dev.auth.ResendEmailResponse;
import kaiquebt.dev.auth.dto.LoginDto;
import kaiquebt.dev.auth.interfaces.IUserSessionLogInstantiator;
import kaiquebt.dev.auth.model.BaseUser;
import kaiquebt.dev.auth.model.BaseUser.RoleType;
import kaiquebt.dev.auth.model.BaseUserSessionLog;
import kaiquebt.dev.auth.repository.BaseUserRepository;
import kaiquebt.dev.auth.service.JwtTokenProvider.GeneratedTokenResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BaseAuthService<T extends BaseUser, U extends BaseUserSessionLog<T>> {
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final BaseUserRepository<T> baseUserRepository;
    private final UserSessionLogService<T, U> userSessionLogService;
    private final IUserSessionLogInstantiator<T, U> sessionInstantiator;
    private final EmailService<T> emailService;

    public interface SignupHook<T extends BaseUser> {
        default void beforeValidation(T user, SignupRequest<T> request) {}
        default void afterValidation(T user, SignupRequest<T> request) {}
        default void beforeSave(T user, SignupRequest<T> request) {}
        default void afterSave(T user, SignupRequest<T> request) {}
        default void onError(Exception error, T user, SignupRequest<T> request) {}
    }
    
    public interface SignupRequest<T extends BaseUser> {
        T getUser();
        Set<RoleType> getRoles();
        SignupHook<T> getHook();
    }

    public String signup(SignupRequest<T> request) {
        T user = request.getUser();        
        SignupHook<T> hook = request.getHook();
        
        try {
            if (hook != null) {
                hook.beforeValidation(user, request);
            }
            
            if (baseUserRepository.existsByUsername(user.getUsername()) || baseUserRepository.existsByUsername(user.getEmail())) {
                throw new IllegalArgumentException("Username already exists!");
            }
            
            // Check if email exists
            if (baseUserRepository.existsByEmail(user.getEmail()) || baseUserRepository.existsByEmail(user.getUsername())) {
                throw new IllegalArgumentException("Email already exists!");
            }
            
            // Hook: After validation
            if (hook != null) {
                hook.afterValidation(user, request);
            }
            
            // Create new user
            // Password will be setted on email confirmation
            // user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            // Set roles from request
            user.setRoles(new HashSet<>(request.getRoles()));
            
            
            emailService.sendMagicLink(user);
            
            // Hook: Before save
            if (hook != null) {
                hook.beforeSave(user, request);
            }
            baseUserRepository.save(user);
            
            // Hook: After save
            if (hook != null) {
                hook.afterSave(user, request);
            }
            
            return "User registered successfully!";
            
        } catch (Exception e) {
            // Hook: On error
            if (hook != null) {
                hook.onError(e, user, request);
            }
            throw e;
        }
    }    

    public String login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsernameOrEmail(),
                        loginDto.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        T user = this.baseUserRepository.findByEmail(authentication.getName()).get();
        GeneratedTokenResponse generateToken = jwtTokenProvider.generateToken(user);
        // loggin session
        userSessionLogService.registerLoginSession(
            user,
            this.sessionInstantiator.instantiate(user)
        );
        return generateToken.token;
    }

    public ResendEmailResponse sendEmailConfirmation(String email) {
        Optional<T> userOpt = this.baseUserRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException(
                "Usuário não encontrado para email: "+(email != null ? email : "")
            );
        }
        
        T user = userOpt.get();
        if (user.getEmailConfirmed()) {
            throw new IllegalArgumentException("Email já foi confirmado");
        }

        long canResentAfter = user.canResendAfter();
        if (canResentAfter != 0) {
            return ResendEmailResponse.builder()
            .after(canResentAfter)
            .message("Email já foi enviado recentemente. Tente novamente em alguns minutos")
            .resended(false)
            .build();
        }

        String token = UUID.randomUUID().toString();

        user.setEmailConfirmationToken(token);
        user.setTokenExpiresAt(LocalDateTime.now().plus(BaseUser.DEFAULT_TOKEN_EXPIRATION));
        emailService.sendMagicLink(user);
        
        this.baseUserRepository.save(user);


        return ResendEmailResponse.builder()
            .after(0L)
            .message("Email de confirmação enviado com sucesso! Verifique sua caixa de entrada")
            .resended(true)
        .build();
    }

    public String confirmEmail(String token) {
        Optional<T> userOpt = this.baseUserRepository.findByEmailConfirmationToken(token);
        
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Token inválido");
        }
        
        T user = userOpt.get();
        if (user.getEmailConfirmed()) {
            // cleaning token
            user.setEmailConfirmationToken(null);
            user.setTokenExpiresAt(null);
            this.baseUserRepository.save(user);
            throw new IllegalArgumentException("Email já foi confirmado");
        }
        
        if (user.getTokenExpiresAt() == null || user.getTokenExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expirado");
        }
        
        user.setEmailConfirmed(true);
        user.setEmailConfirmationToken(null);
        user.setTokenExpiresAt(null);
        this.baseUserRepository.save(user);
        
        return jwtTokenProvider.generateToken(user).token;
    }

    public void defineFirstPassword(Long id, String password) {
        Optional<T> userOpt = this.baseUserRepository.findById(id);
        
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        
        T user = userOpt.get();
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Senha já foi definida anteriormente");
        }

        user.setPassword(passwordEncoder.encode(password));
        this.baseUserRepository.save(user);
    }

    public String sendRecoverEmail(String email) {
        Optional<T> userOpt = this.baseUserRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException(
                "Usuário não encontrado para email: "+(email != null ? email : "")
            );
        }
        
        T user = userOpt.get();
        if (!user.getEmailConfirmed()) {
            throw new IllegalArgumentException("Email não foi confirmado");
        }

        String token = UUID.randomUUID().toString();
        
        // Last recover must be more than 1 day ago
        boolean hasActiveRecover = user.getPasswordRecoverExpiration() != null && user.getPasswordRecoverExpiration().isAfter(LocalDateTime.now());
        if (
            !hasActiveRecover &&
            user.getPasswordRecoverExpiration() != null && user.getPasswordRecoverExpiration().isAfter(LocalDateTime.now().minusDays(1))
        ) {
            return "Um email de recuperação já foi enviado recentemente. Tente novamente mais tarde. O limite é de 1 recuperação a cada 24 horas.";
        }
        if (hasActiveRecover) {
            return null;
        }

        user.setPasswordRecoverToken(token);
        user.setPasswordRecoverExpiration(LocalDateTime.now().plus(BaseUser.DEFAULT_TOKEN_EXPIRATION));
        

        
        emailService.sendRecoverEmail(user);
        
        this.baseUserRepository.save(user);
        return null;
    }
}

package kaiquebt.dev.auth.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            
            // Set roles from request
            user.setRoles(new HashSet<>(request.getRoles()));
            
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
}

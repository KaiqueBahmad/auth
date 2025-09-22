package kaiquebt.dev.auth.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import kaiquebt.dev.auth.model.BaseUser;
import kaiquebt.dev.auth.model.BaseUser.RoleType;
import kaiquebt.dev.auth.repository.BaseUserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BaseAuthService<T extends BaseUser> {
    private final BaseUserRepository<T> repository;
    private final PasswordEncoder passwordEncoder;

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
            
            if (repository.existsByUsername(user.getUsername()) || repository.existsByUsername(user.getEmail())) {
                throw new IllegalArgumentException("Username already exists!");
            }
            
            // Check if email exists
            if (repository.existsByEmail(user.getEmail()) || repository.existsByEmail(user.getUsername())) {
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
            
            repository.save(user);
            
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
    
}

package kaiquebt.dev.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import kaiquebt.dev.auth.model.BaseUser;
import kaiquebt.dev.auth.repository.BaseUserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService<T extends BaseUser> implements UserDetailsService {

    private final BaseUserRepository<T> baseUserRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        BaseUser user = baseUserRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> 
                        new UsernameNotFoundException("User not found: " + usernameOrEmail));

        return CustomUserDetails.fromUser(user);
    }
}
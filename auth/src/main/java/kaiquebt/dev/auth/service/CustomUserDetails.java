package kaiquebt.dev.auth.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import kaiquebt.dev.auth.model.BaseUser;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    
    private final BaseUser user;
    private final Collection<? extends GrantedAuthority> authorities;
    
    public static CustomUserDetails fromUser(BaseUser user) {
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());
                
        return new CustomUserDetails(user, authorities);
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

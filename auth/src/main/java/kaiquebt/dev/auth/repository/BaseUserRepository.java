package kaiquebt.dev.auth.repository;

import kaiquebt.dev.auth.model.BaseUser;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BaseUserRepository <T extends BaseUser> extends JpaRepository<T, Long> {
    Optional<T> findByUsername(String username);

    Optional<T> findByEmail(String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Optional<T> findByUsernameOrEmail(String username, String email);

    Optional<T> findByEmailConfirmationToken(String token);
}

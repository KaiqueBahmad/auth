package kaiquebt.dev.client.repository;

import org.springframework.stereotype.Repository;
import kaiquebt.dev.auth.repository.BaseUserRepository;
import kaiquebt.dev.client.model.User;


@Repository
public interface UserRepository extends BaseUserRepository<User> {
}

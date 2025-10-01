# Auth Template Lib

This lib will help you to faster implement auth on SpringBoot Project

## Dependencies:
- Spring Boot (3.5.6)
- Postgres (Planning to remove this dependencie)

## Getting Started
In order to start using this Auth Template you must set some variables on your application.properties, configure some beans for customazing the behaviour of the template, and implement some JPA Entities, on client folder there is a full implementation that may help you.

### Application properties
```
spring.application.name=client
server.port=8081
# Database connection
spring.datasource.url=jdbc:postgresql://localhost:5432/auth-template
spring.datasource.username=postgres
spring.datasource.password=postgres

spring.jpa.hibernate.ddl-auto=update

# Should be 64 characters long
kaiquebt.dev.auth.jwt-secret=yoursecret
kaiquebt.dev.auth.jwt-expiration-milliseconds=86400000

#Base path for the routes create by the template
kaiquebt.dev.auth.base-path=/api/auth/

# Because we implement email confirmation we need a "external url"
# it is, on the email we will send a link based on it in order to confirm account
# if your api is avaliable through "yourdomain.com/api" this is your external url
# on dev environment you can use ngrok or just set it to http://localhost:<PORT>
kaiquebt.dev.auth.external-url=

# Email configuration for enabling the email confirmation features
spring.mail.host=
spring.mail.port=
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.ssl.enable=
spring.mail.properties.mail.smtp.starttls.enable=
```

### Routes created by the template
> **Note:** The signup/registration route is not included in this template and must be implemented by the user. See the [Implementing Signup](#implementing-signup) section for more details.

**POST `/api/auth/login`** - Authenticates users with credentials and returns a JWT access token.

**POST `/api/auth/resend-email`** - Resends the email confirmation message to a specified email address.

**Under development:**

**GET `/api/auth/check-email`** - Checks if the authenticated user's email has been confirmed.

**POST `/api/auth/refresh`** - Refreshes an expired JWT token. Supports admin impersonation by handling both user and impersonator tokens simultaneously.

**POST `/api/auth/recover-account/send-email`** - Initiates password recovery by sending a verification code to the user's email.

**POST `/api/auth/recover-account/verify`** - Validates the recovery token sent to the user's email.

**POST `/api/auth/recover-account/change-password`** - Completes the password recovery process by setting a new password after token verification.

### Models to be implemented
> In order to manage the persistence while keeping you free to personalize the entities and behaviour, we just declare Mapped Superclasses instead of real implementations, that would limit the user of the lib

- #### Base User
  
  You must implement BaseUser (that is declared on the lib)
  
  also is needed to implement its repository

  you can add whatever new fields your application may need here

  Example of implementations:
  
  ##### Entity
  ```java
  @Entity
  @Table(name = "users")
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @SuperBuilder
  @ToString
  @EqualsAndHashCode(callSuper = true)
  public class User extends BaseUser {
      
      @Column(name = "other", nullable = true)
      private String other;
  }
  ```
  ##### Repository
  ```java
  @Repository
  public interface UserRepository extends BaseUserRepository<User> {
  }
  ```
- #### Base User Log
  
  The template keeps a log of every sucessfull login, keep the ip of the user, and (as we support impersonating) also stores the admin that was on that account (if the login was not made by the user)

  You can also add new fields to this class

  Example of implementations:
  
  ##### Entity
  ```java
  @SuperBuilder
  @Data
  @ToString
  @NoArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  @Entity
  @Table(name = "user_session_logs")
  public class UserSessionLog extends BaseUserSessionLog<User> {
      
  }
  ```
  ##### Repository
  ```java
  @SuperBuilder
  @Data
  @ToString
  @NoArgsConstructor
  @EqualsAndHashCode(callSuper = true)
  @Entity
  @Table(name = "user_session_logs")
  public class UserSessionLog extends BaseUserSessionLog<User> {
      
  }
  ```
- #### Email Template Bean

  will be used on the building of the emails that will be sent to the user. 

  Example of implementations:
  ```java
  @Service
  public class EmailTemplateBean implements IEmailTemplateBean<User> {
      
      @Override
      public String build(User user, String confirmationUrl) {
          return String.format("""
              your html template here
              """, user.getUsername(), confirmationUrl, confirmationUrl, confirmationUrl);
      }
      
      @Override
      public String getEmailConfirmTitle() {
          return "Client of kaiquebt auth module :D";
      }
  }
  ```

### Implementing Signup

The signup/registration functionality is not pre-configured in the controller but can be implemented using the `BaseAuthService.signup()` method.
This requires a `SignupRequest` that defines the new user, their roles, and an optional `SignupHook` for custom behavior.

#### HookHandler

The `HookHandler` class implements `SignupHook<User>` and allows you to intercept different stages of the signup process:

```java
private static class HookHandler implements SignupHook<User> {
    private static HookHandler instance = new HookHandler();
    public static HookHandler getInstance() { return instance; }
    private HookHandler() {}

    @Override
    public void beforeSave(User user, SignupRequest<User> request) {
        user.setEmailConfirmationToken(UUID.randomUUID().toString());
        user.setEmailConfirmed(false);
        user.setPasswordRecoverTries(1);
    }

    // Other lifecycle methods can be overridden as needed:
    // beforeValidation(), afterValidation(), afterSave(), onError(Exception, User, SignupRequest)
}
```

**Purpose:**
- `beforeSave()` is used here to set initial fields like `emailConfirmationToken`, `emailConfirmed`, and `passwordRecoverTries`.
- Override other lifecycle methods (`beforeValidation`, `afterValidation`, `afterSave`, `onError`) to add validation steps, side-effects (emails, logs), or custom error handling.

#### Example Signup Endpoint

```java
@GetMapping("/criar")
public String createUser() {
    User user = User.builder()
        .email("kaiquebahmadt@gmail.com")
        .other("aeiou")
        .password("senhaforte")
        .username("kaiquebt")
        .build();

    SignupRequest<User> request = new SignupRequest<User>() {
        @Override public User getUser() { return user; }
        @Override public SignupHook<User> getHook() { return HookHandler.getInstance(); }
        @Override public Set<RoleType> getRoles() { return Set.of(RoleType.ROLE_USER); }
    };

    service.signup(request);
    return "User registered";
}
```

#### Key Points
- `BaseAuthService.signup()` handles user creation.
- `SignupRequest` provides the user object, assigned roles, and the signup hook.
- `SignupHook` provides lifecycle hooks to add custom logic during signup (init fields, send confirmation emails, audit logs, error handling).

---


#### Security Configuration

The application also requires a `SecurityConfig` class.  
This configuration **depends heavily on business rules**, so it should be customized according to the authentication and authorization needs of your project.

Example skeleton:

```java
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter authenticationFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public IUserSessionLogInstantiator<User, UserSessionLog> instantiator() {
        return user -> new UserSessionLog();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(
                    authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().permitAll()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

- Use `SecurityFilterChain` to define which endpoints are public and which require authentication.  
- Insert JWT authentication filters (or another strategy) as needed.  
- Adjust authorization rules according to your business logic.
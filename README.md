# Auth Template Lib

This library helps you quickly implement authentication in Spring Boot projects with built-in email confirmation, password recovery, and session logging.

## Features

- JWT-based authentication
- Email confirmation workflow
- Password recovery system
- Session logging with IP tracking
- Customizable validation and hooks
- Support for user impersonation (admin features)

## Dependencies

- Spring Boot (3.5.6)
- Spring Security
- Spring Data JPA
- Spring Mail
- JWT (io.jsonwebtoken)

## Getting Started

To use this Auth Template, you need to:
1. Configure application properties
2. Implement required JPA entities
3. Create required beans
4. Configure Spring Security

The `client` folder contains a complete reference implementation.

---

## Application Properties

```properties
spring.application.name=your-app-name
server.port=8081

# Database connection
spring.datasource.url=jdbc:postgresql://localhost:5432/your-database
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update

# JWT Configuration (secret must be 64 characters long)
kaiquebt.dev.auth.jwt-secret=your-64-character-secret-key-here
kaiquebt.dev.auth.jwt-expiration-milliseconds=86400000

# Base path for authentication routes
kaiquebt.dev.auth.base-path=/api/auth/

# External URL for email links
# This is used to generate confirmation links in emails
# In production: https://yourdomain.com
# In development: http://localhost:8081 or use ngrok
kaiquebt.dev.auth.external-url=http://localhost:8081

# Email Configuration (Spring Mail)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.ssl.enable=false
spring.mail.properties.mail.smtp.starttls.enable=true

# Optional: Override email config specifically for auth module
# Use this if you want different email settings for auth vs other app features
kaiquebt.dev.auth.mail.host=
kaiquebt.dev.auth.mail.port=
kaiquebt.dev.auth.mail.username=
kaiquebt.dev.auth.mail.password=
kaiquebt.dev.auth.mail.properties.mail.smtp.ssl.enable=
kaiquebt.dev.auth.mail.properties.mail.smtp.starttls.enable=
```

---

## Authentication Routes

The template automatically creates the following routes:

### **POST `/api/auth/login`**
Authenticates users and returns a JWT access token.

**Request Body:**
```json
{
  "usernameOrEmail": "user@example.com",
  "password": "userPassword"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login realizado com sucesso",
  "data": {
    "accessToken": "jwt-token-here"
  }
}
```

---

### **POST `/api/auth/resend-email`**
Resends the email confirmation message.

**Query Parameter:** `email`

**Response:**
```json
{
  "success": true,
  "message": "Email de confirmação enviado com sucesso!",
  "data": {
    "resended": true,
    "after": 0
  }
}
```

---

### **GET `/api/auth/confirm-email`**
Confirms the user's email address and returns a JWT token for the session.

**Query Parameter:** `token`

**Response:**
```json
{
  "success": true,
  "message": "Email confirmado com sucesso",
  "data": {
    "token": "jwt-token-here"
  }
}
```

**Important:** After email confirmation, the user receives a JWT token but still needs to set their password using the `/define-first-password` endpoint.

---

### **POST `/api/auth/define-first-password`**
Sets the user's password after email confirmation. Requires authentication with the token from email confirmation.

**Headers:**
```
Authorization: Bearer <token-from-email-confirmation>
```

**Request Body:**
```json
{
  "password": "newSecurePassword123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Senha definida com sucesso",
  "data": null
}
```

---

### **POST `/api/auth/recover-account/send-email`**
Initiates password recovery by sending a recovery token to the user's email.

**Query Parameter:** `email`

**Response:**
```json
{
  "success": true,
  "message": "Se o email existir em nossa base de dados...",
  "data": null
}
```

**Note:** Password recovery routes are partially implemented. The verify and change-password endpoints are marked for future development.

---

## Required Implementations

### 1. User Entity

Extend `BaseUser` to create your user entity:

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
    
    // Add your custom fields here
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;
}
```

**User Repository:**

```java
@Repository
public interface UserRepository extends BaseUserRepository<User> {
    // Add custom queries if needed
}
```

---

### 2. User Session Log Entity

Extend `BaseUserSessionLog` to track login sessions:

```java
@SuperBuilder
@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_session_logs")
public class UserSessionLog extends BaseUserSessionLog<User> {
    // Add custom fields if needed
}
```

**Session Log Repository:**

```java
@Repository
public interface UserSessionLogRepository extends BaseUserSessionLogRepository<UserSessionLog> {
}
```

---

### 3. Email Template Bean

Implement email templates for confirmation and password recovery:

```java
@Service
public class EmailTemplateBean implements IEmailTemplateBean<User> {
    
    @Override
    public String buildEmailConfirm(User user, String confirmationUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .button { background-color: #4CAF50; color: white; padding: 14px 20px; 
                             text-decoration: none; border-radius: 4px; display: inline-block; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>Welcome, %s!</h2>
                    <p>Thank you for registering. Please confirm your email address by clicking the button below:</p>
                    <a href="%s" class="button">Confirm Email</a>
                    <p>Or copy this link: %s</p>
                    <p>This link will expire in 10 minutes.</p>
                </div>
            </body>
            </html>
            """, user.getUsername(), confirmationUrl, confirmationUrl);
    }
    
    @Override
    public String getEmailConfirmTitle() {
        return "Confirm Your Email Address";
    }
    
    @Override
    public String buildRecoverAccount(User user, String recoverToken) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .token { background-color: #f0f0f0; padding: 10px; font-size: 18px; 
                            font-weight: bold; text-align: center; border-radius: 4px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>Password Recovery</h2>
                    <p>Hello, %s!</p>
                    <p>You requested to recover your password. Use the following token:</p>
                    <div class="token">%s</div>
                    <p>This token will expire in 10 minutes.</p>
                    <p>If you didn't request this, please ignore this email.</p>
                </div>
            </body>
            </html>
            """, user.getUsername(), recoverToken);
    }
    
    @Override
    public String getRecoverAccountTitle() {
        return "Password Recovery Request";
    }
}
```

---

### 4. Security Configuration

Configure Spring Security with JWT authentication:

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
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        // Add your protected routes here
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

---

## Implementing User Signup

The signup functionality is not pre-configured as a controller endpoint, allowing you to customize the registration flow. Use the `BaseAuthService.signup()` method with a `SignupRequest`.

### SignupHook Interface

The `SignupHook` provides lifecycle hooks to customize the signup process:

```java
private static class SignupHookHandler implements SignupHook<User> {
    private static final SignupHookHandler INSTANCE = new SignupHookHandler();
    
    public static SignupHookHandler getInstance() {
        return INSTANCE;
    }
    
    private SignupHookHandler() {}

    @Override
    public void customValidation(User user, SignupRequest<User> request) throws IllegalArgumentException {
        // Add custom validation logic
        // Example: validate phone number format, age restrictions, etc.
        if (user.getUsername().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters");
        }
    }

    @Override
    public void beforeSave(User user, SignupRequest<User> request) {
        // Initialize fields before saving to database
        // The library handles email confirmation token creation
        
        // Example: set default values
        user.setRoles(Set.of(RoleType.ROLE_USER));
    }

    @Override
    public void afterSave(User user, SignupRequest<User> request) {
        // Perform actions after user is saved
        // Example: send welcome notification, create user profile, log analytics
        System.out.println("New user registered: " + user.getEmail());
    }

    @Override
    public void onError(Exception error, User user, SignupRequest<User> request) {
        // Handle errors during signup
        // Example: log errors, send alerts, cleanup resources
        System.err.println("Signup error for " + user.getEmail() + ": " + error.getMessage());
    }
}
```

### Example Signup Endpoint

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final BaseAuthService<User, UserSessionLog> authService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterDto dto) {
        User user = User.builder()
            .email(dto.getEmail())
            .username(dto.getUsername())
            .phoneNumber(dto.getPhoneNumber())
            .build();

        SignupRequest<User> request = new SignupRequest<User>() {
            @Override
            public User getUser() {
                return user;
            }

            @Override
            public SignupHook<User> getHook() {
                return SignupHookHandler.getInstance();
            }
        };

        try {
            String message = authService.signup(request);
            return ResponseEntity.ok(message);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
```

---

## User Registration Flow

Understanding the complete registration flow:

1. **User Registration**
   - Call `authService.signup()` with user details
   - User is saved to database without a password
   - Email confirmation token is generated and stored
   - Confirmation email is sent automatically

2. **Email Confirmation**
   - User clicks link in email
   - `GET /api/auth/confirm-email?token=...` is called
   - Email is marked as confirmed
   - JWT token is returned

3. **Password Setup**
   - User uses JWT token from step 2
   - Calls `POST /api/auth/define-first-password` with new password
   - Password is hashed and stored
   - User can now login normally

4. **Regular Login**
   - User provides email/username and password
   - `POST /api/auth/login` returns JWT token
   - Token is used for authenticated requests

---

## Optional: Custom Password Validation

By default, the library accepts any non-empty password. To add custom validation:

```java
@Service
public class CustomPasswordValidator implements IPasswordValidator {
    
    @Override
    public void doValidate(String password) throws IllegalArgumentException {
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one number");
        }
        
        if (!password.matches(".*[!@#$%^&*()].*")) {
            throw new IllegalArgumentException("Password must contain at least one special character");
        }
    }
}
```

This bean will automatically be picked up by Spring and used for password validation.

---

## Session Logging

The library automatically logs all successful authentication events, including:

- Login events (with IP address and user agent)
- Token refresh events
- Admin impersonation events

Access session history through the `UserSessionLogService`:

```java
@Autowired
private UserSessionLogService<User, UserSessionLog> sessionLogService;

public Page<SessionHistoryDto> getUserSessions(Long userId, int page, int size) {
    return sessionLogService.getSessionHistory(userId, page, size, null, null);
}
```

---

## User Roles

The library includes predefined role types:

- `ROLE_USER` - Standard user
- `ROLE_ADMIN` - Administrator
- `ROLE_BANNED` - Banned user

Roles are stored in the `user_roles` table and can be checked using Spring Security's `@PreAuthorize` annotation:

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/users")
public List<User> getAllUsers() {
    // Only accessible by admins
}
```

---

## Token Structure

JWT tokens include the following claims:

- `sub` (subject): User's email
- `username`: User's username
- `email`: User's email
- `roles`: Array of user roles
- `iat` (issued at): Token creation timestamp
- `exp` (expiration): Token expiration timestamp

---

## Error Handling

The library uses `StandardResponse` wrapper for consistent API responses:

```java
{
  "success": boolean,
  "message": "string",
  "data": object | null
}
```

Common HTTP status codes:
- `200 OK`: Successful operation
- `400 Bad Request`: Validation error or invalid input
- `401 Unauthorized`: Invalid credentials
- `500 Internal Server Error`: Server error

---

## Testing

See `ClientApplicationTests.java` for comprehensive test examples covering:

- User registration with email sending
- Email confirmation flow
- Duplicate username/email detection
- Invalid email format validation
- Expired token handling
- Password definition

---

## Troubleshooting

### Email not sending
- Check SMTP credentials in `application.properties`
- Verify firewall/network allows SMTP connections
- For Gmail: enable "Less secure app access" or use App Passwords
- Check logs for `JavaMailSender` errors

### JWT validation failing
- Ensure `jwt-secret` is exactly 64 characters
- Check token expiration settings
- Verify `Authorization: Bearer <token>` header format

## Future Features (Under Development)

- Token refresh endpoint
- Complete password recovery flow with token verification
- Password change for already-confirmed accounts
- Admin impersonation features

---

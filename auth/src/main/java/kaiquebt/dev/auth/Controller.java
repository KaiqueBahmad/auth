package kaiquebt.dev.auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kaiquebt.dev.auth.dto.JwtAuthResponse;
import kaiquebt.dev.auth.dto.LoginDto;
import kaiquebt.dev.auth.service.BaseAuthService;
import kaiquebt.dev.auth.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequestMapping("${kaiquebt.dev.auth.base-path:/api/auth}")
@RequiredArgsConstructor
@Slf4j
public class Controller {
    
    // Standard response classes
    public static class StandardResponse<T> {
        private boolean success;
        private String message;
        private T data;
        
        public StandardResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
    }
    
    private final BaseAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<StandardResponse<JwtAuthResponse>> login(@RequestBody LoginDto loginDto) {
        try {
            String token = authService.login(loginDto);
            JwtAuthResponse response = new JwtAuthResponse();
            response.setAccessToken(token);
            
            return ResponseEntity.ok(new StandardResponse<>(
                true, 
                "Login realizado com sucesso", 
                response
            ));
        } catch (Exception e) {
            log.error("Error on login", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new StandardResponse<>(
                    false,
                    "Credenciais inválidas",
                    null
                )
            );
        }
    }
    
    // @PostMapping("/signup")
    // public ResponseEntity<?> signup(@RequestBody SignuDto signupDto) {
    //     String response = authService.signup(signupDto);
    //     return new ResponseEntity<>(Map.of("message", response), HttpStatus.CREATED);
    // }
    

    @PostMapping("/resend-email")
    public ResponseEntity<StandardResponse<ResendEmailResponse>> resendEmail(@RequestParam String email) {
        if (email == null || email.strip().isEmpty()) {
            return ResponseEntity.badRequest().body(
                new StandardResponse<>(
                    false,
                    "Email é obrigatório",
                    null
                )
            );
        }
        try {
            ResendEmailResponse response = authService.sendEmailConfirmation(email);
            return ResponseEntity.ok(new StandardResponse<>(
                true,
                response.getMessage(),
                response
            ));            
        } catch (Exception e) {
            log.error("Error on resend email confirmation to "+email, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new StandardResponse<>(
                    false,
                    e.getMessage(),
                    null
                )
            );
        }
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<StandardResponse<ConfirmEmailResponse>> confirmEmail(@RequestParam String token) {
        try {
            String jwtToken = this.authService.confirmEmail(token);
            return ResponseEntity.ok(new StandardResponse<>(
                true,
                "Email confirmado com sucesso",
                ConfirmEmailResponse.builder().token(jwtToken).build()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new StandardResponse<>(
                    false,
                    e.getMessage(),
                    null
                )
            );
        } catch (Exception e) {
            log.error("Error on confirm email with token "+token, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new StandardResponse<>(
                    false,
                    "Ocorreu um erro ao confirmar o email. Tente novamente mais tarde",
                    null
                )
            );
        }
    }

    @PostMapping("/define-first-password")
    public ResponseEntity<StandardResponse<String>> defineFirstPassword(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody SetFirstPasswordDto dto
    ) {
        // Should only work if was never set a password on this account
        try {
            this.authService.defineFirstPassword(userDetails.getUser().getId(), dto.getPassword());
            return ResponseEntity.ok(
                new StandardResponse<>(
                    true,
                    "Senha definida com sucesso",
                    null
                )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new StandardResponse<>(
                    false,
                    e.getMessage(),
                    null
                )
            );
        } catch (Exception e) {
            log.error("Error on define first password for user id "+userDetails.getUser().getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new StandardResponse<>(
                    false,
                    "Ocorreu um erro ao definir a senha. Tente novamente mais tarde",
                    null
                )
            );
        }
    }

    @PostMapping("recover-account/send-email")
    public ResponseEntity<StandardResponse<String>> sendRecoverEmail(
        @RequestParam String email
    ) {
        // Should not work while the account is not confirmed
        try {
            String importantAdvice = this.authService.sendRecoverEmail(email);
            String message;
            if (importantAdvice != null && !importantAdvice.isEmpty()) {
                message = importantAdvice;
            } else {
                message = "Se o email existir em nossa base de dados, e seu email estiver confirmado, você receberá um email com instruções para recuperar sua conta";
            }
            return ResponseEntity.ok(
                new StandardResponse<>(
                    true,
                    message,
                    null
                )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new StandardResponse<>(
                    false,
                    e.getMessage(),
                    null
                )
            );
        } catch (Exception e) {            
            log.error("Error on send recover email to "+email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new StandardResponse<>(
                    false,
                    "Ocorreu um erro ao enviar o email de recuperação. Tente novamente mais tarde",
                    null
                )
            );
        }
    }

    @PostMapping("recover-account/verify")
    public ResponseEntity<String> verifyToken(
        // @RequestBody VerifyRecoverTokenDto dto
    ) {
        // Verify if the token for recovering account is valid
        // maybe if it is we give some seconds extra of expiration for the token 
        return null;
    }

    @PostMapping("recover-account/change-password")
    public ResponseEntity<String> changePassword(
        // @RequestBody ChangePasswordDto dto
    ) {
        // should not work if the account is not confirmed
        return null;
    }

    // @PostMapping("refresh")
    // public ResponseEntity<JwtAuthResponse> refresh(@RequestBody RefreshJWTDto dto) {
    //     JwtAuthResponse response = new JwtAuthResponse();
    //     if (dto.getImpersonatorToken() != null && !dto.getImpersonatorToken().strip().isEmpty()) {
    //         String[] newTokens = this.authService.refreshWithImpersonator(dto.getToken(), dto.getImpersonatorToken());            
    //         response.setAccessToken(newTokens[0]);
    //         response.setPerformerToken(newTokens[1]);
    //     } else {
    //         String token = this.authService.refresh(dto.getToken());
    //         response.setAccessToken(token);
    //     }
               
    //     return ResponseEntity.ok(response);
    // }    
}

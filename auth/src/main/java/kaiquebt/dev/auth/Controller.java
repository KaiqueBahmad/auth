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

    private final BaseAuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody LoginDto loginDto) {
        String token = authService.login(loginDto);
        JwtAuthResponse response = new JwtAuthResponse();
        response.setAccessToken(token);

        return ResponseEntity.ok(response);
    }
    
    // @PostMapping("/signup")
    // public ResponseEntity<?> signup(@RequestBody SignuDto signupDto) {
    //     String response = authService.signup(signupDto);
    //     return new ResponseEntity<>(Map.of("message", response), HttpStatus.CREATED);
    // }
    

    @PostMapping("/resend-email")
    public ResponseEntity<ResendEmailResponse> resendEmail(@RequestParam String email) {
        if (email == null || email.strip().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        try {
            ResendEmailResponse response = authService.sendEmailConfirmation(email);
            return ResponseEntity.ok(response);            
        } catch (Exception e) {
            log.error("Error on resend email confirmation to "+email);
            log.error(e.toString());
            return ResponseEntity.ok(ResendEmailResponse.builder()
                .after(0L)
                .message("Email de confirmação enviado com sucesso! Verifique sua caixa de entrada")
                .resended(true)
                .build()
            );
        }
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<?> confirmEmail(@RequestParam String token) {
        try {
            return ResponseEntity.ok(ConfirmEmailResponse.builder()
                .token(this.authService.confirmEmail(token))
                .build()
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of(
                    "error", e.toString(),
                    "message", e.getMessage()
                )
            );
        } catch (Exception e) {
            log.error("Error on confirm email with token "+token, e);
            return ResponseEntity.internalServerError().body(
                Map.of(
                    "error", "Ocorreu um erro ao confirmar o email. Tente novamente mais tarde",
                    "message", "Ocorreu um erro ao confirmar o email. Tente novamente mais tarde"
                )
            );
        }
    }

    @PostMapping("/define-first-password")
    public ResponseEntity<?> defineFirstPassword(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody SetFirstPasswordDto dto
    ) {
        // Should only work if was never set a password on this account
        try {
            this.authService.defineFirstPassword(userDetails.getUser().getId(), dto.getPassword());
            return ResponseEntity.ok(
                Map.of(
                    "message", "Senha definida com sucesso"
                )
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of(
                    "error", e.toString(),
                    "message", e.getMessage()
                )
            );
        } catch (Exception e) {
            log.error("Error on define first password for user id "+userDetails.getUser().getId(), e);
            return ResponseEntity.internalServerError().body(
                Map.of(
                    "error", "Ocorreu um erro ao definir a senha. Tente novamente mais tarde",
                    "message", "Ocorreu um erro ao definir a senha. Tente novamente mais tarde"
                )
            );
        }
    }

    @PostMapping("recover-account/send-email")
    public ResponseEntity<?> sendRecoverEmail(
        @RequestBody SendRecoverEmailDto dto
    ) {
        // Should not work while the account is not confirmed
        try {
            String importantAdvice = this.authService.sendRecoverEmail(dto.getEmail());
            if (importantAdvice != null && !importantAdvice.isEmpty()) {
                return ResponseEntity.ok(
                    Map.of(
                        "message", importantAdvice
                    )
                );
            }
        } catch (IllegalArgumentException e) {
            
        } catch (Exception e) {            
            log.error("Error on send recover email to "+dto, e);
        }
        
        return ResponseEntity.ok(
            Map.of(
                "message", "Se o email existir em nossa base de dados, e seu email estiver confirmado, você receberá um email com instruções para recuperar sua conta"
            )
        );
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

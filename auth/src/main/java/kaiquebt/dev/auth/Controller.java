package kaiquebt.dev.auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kaiquebt.dev.auth.dto.JwtAuthResponse;
import kaiquebt.dev.auth.dto.LoginDto;
import kaiquebt.dev.auth.service.BaseAuthService;
import kaiquebt.dev.auth.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("${kaiquebt.dev.auth.base-path:/api/auth}")
@RequiredArgsConstructor
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
        // trigger resend email confirmation, should only work if account is not confirmed
        return null;
    }

    @GetMapping("/confirm-email")
    public ResponseEntity<ResendEmailResponse> confirmEmail(@RequestParam String emailConfirmationToken) {
        // magic link that confirms the account and give a single time use JWT, then we can use the define-first-password
        return null;
    }

    @PostMapping("/define-first-password")
    public String defineFirstPassword(
        @AuthenticationPrincipal CustomUserDetails userDetails
        // @RequestBody SetFirstPasswordDto dto
    ) {
        // Should only work if was never set a password on this account
        return null;
    }

    @PostMapping("recover-account/send-email")
    public ResponseEntity<String> sendRecoverEmail(
        // @RequestBody SendRecoverEmailDto dto
    ) {
        // Should not work while the account is not confirmed
        return null;
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

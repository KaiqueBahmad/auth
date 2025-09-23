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
    public ResponseEntity<ResendEmailResponse> resendEmail(@AuthenticationPrincipal CustomUserDetails userDetails) {
        ResendEmailResponse response = authService.sendEmailConfirmation(userDetails);
        return ResponseEntity.ok(response);
    }


    // @GetMapping("/check-email")
    // public YesOrNo checkEmail(@AuthenticationPrincipal CustomUserDetails userDetails) {
    //     boolean confirmed = authService.checkEmail(userDetails.getUser());
    //     return YesOrNo.builder().status(confirmed).build();
    // }

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

    // @PostMapping("recover-account/send-email")
    // public ResponseEntity<String> sendRecoverEmail(@RequestBody SendRecoverEmailDto dto) {
        
    //     String response = this.authService.recoverAccountSendEmail(dto.getEmail());

    //     if (response == null) {
    //         return ResponseEntity.ok("Verifique seu email para o código de verificação");
    //     } else {
    //         return ResponseEntity.badRequest().body(response);
    //     }

    // }

    // @PostMapping("recover-account/verify")
    // public ResponseEntity<String> verifyToken(@RequestBody VerifyRecoverTokenDto dto) {
    //     return this.authService.verifyToken(dto.getEmail(), dto.getToken());
    // }

    // @PostMapping("recover-account/change-password")
    // public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDto dto) {
    //     if (!dto.isPasswordValid()) {
    //         return ResponseEntity.badRequest().body(dto.getPasswordError());
    //     }

    //     return this.authService.changePassword(dto.getEmail(), dto.getToken(), dto.getPassword());
    // }
    
}

package co.com.kura.b2c.api.controller;

import co.com.kura.b2c.api.dto.*;
import co.com.kura.b2c.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/otp/send")
    public ResponseEntity<Map<String, String>> sendOtp(@Valid @RequestBody OtpRequest request) {
        authService.sendOtp(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody OtpVerifyRequest request) {
        authService.verifyOtp(request.getEmail(), request.getCode());
        return ResponseEntity.ok(Map.of("message", "OTP verified successfully"));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        
        ResponseCookie cookie = ResponseCookie.from("KURA_SESSION", response.getToken())
                .httpOnly(true)
                .secure(true)
                .domain(".kura.com.co")
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofHours(24))
                .build();
        
        response.setToken(null);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        
        ResponseCookie cookie = ResponseCookie.from("KURA_SESSION", response.getToken())
                .httpOnly(true)
                .secure(true)
                .domain(".kura.com.co")
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofHours(24))
                .build();
        
        response.setToken(null);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "If the email exists, a reset link has been sent"));
    }

    @PostMapping("/password/reset/confirm")
    public ResponseEntity<Map<String, String>> confirmPasswordReset(@RequestBody PasswordResetConfirmRequest request) {
        authService.confirmPasswordReset(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}

package co.com.kura.b2c.service;

import co.com.kura.b2c.api.dto.*;
import co.com.kura.b2c.domain.entity.User;
import co.com.kura.b2c.domain.repository.UserRepository;
import co.com.kura.b2c.infrastructure.EmailProvider;
import co.com.kura.b2c.infrastructure.IdentityVerificationProvider;
import co.com.kura.b2c.infrastructure.VerificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmailProvider emailProvider;
    private final IdentityVerificationProvider identityVerificationProvider;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(30);
    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String RESET_KEY_PREFIX = "reset:";

    public void sendOtp(String email) {
        String code = generateOtpCode();
        String redisKey = OTP_KEY_PREFIX + email;
        
        redisTemplate.opsForValue().set(redisKey, code, OTP_TTL);
        
        String subject = "Your KURA Verification Code";
        String body = String.format("Your verification code is: %s\n\nThis code expires in 5 minutes.", code);
        
        emailProvider.sendEmail(email, subject, body);
        log.info("OTP sent to email: {}", email);
    }

    public void verifyOtp(String email, String code) {
        String redisKey = OTP_KEY_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(redisKey);
        
        if (storedCode == null) {
            throw new IllegalArgumentException("OTP expired or not found");
        }
        
        if (!storedCode.equals(code)) {
            throw new IllegalArgumentException("Invalid OTP code");
        }
        
        redisTemplate.delete(redisKey);
        log.info("OTP verified successfully for email: {}", email);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.isConsentLey1581()) {
            throw new IllegalArgumentException("Consent to Ley 1581 is required");
        }
        
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        if (userRepository.existsByCedulaAndDeletedAtIsNull(request.getCedula())) {
            throw new IllegalArgumentException("Cedula already registered");
        }
        
        VerificationResult verification = identityVerificationProvider.verify(
            request.getCedula(), 
            request.getFullName()
        );
        
        if (!verification.match()) {
            throw new IllegalArgumentException("Identity verification failed: " + verification.message());
        }
        
        User user = User.builder()
            .cedula(request.getCedula())
            .fullName(request.getFullName())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .phone(request.getPhone())
            .role("PATIENT")
            .consentLey1581(true)
            .consentDate(OffsetDateTime.now())
            .build();
        
        User savedUser = userRepository.save(user);
        
        log.info("User registered successfully: {}", savedUser.getEmail());
        
        return AuthResponse.builder()
            .userId(savedUser.getId())
            .email(savedUser.getEmail())
            .fullName(savedUser.getFullName())
            .token(generatePlaceholderToken(savedUser.getId()))
            .role(savedUser.getRole())
            .build();
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        return AuthResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .token(generatePlaceholderToken(user.getId()))
            .role(user.getRole())
            .build();
    }

    public void requestPasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmailAndDeletedAtIsNull(email);
        
        if (userOpt.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return;
        }
        
        String token = UUID.randomUUID().toString();
        String redisKey = RESET_KEY_PREFIX + token;
        
        redisTemplate.opsForValue().set(redisKey, email, RESET_TOKEN_TTL);
        
        String subject = "KURA Password Reset Request";
        String resetLink = "https://kura.com.co/reset-password?token=" + token;
        String body = String.format(
            "You requested a password reset. Click the link below to reset your password:\n\n%s\n\n" +
            "This link expires in 30 minutes. If you didn't request this, please ignore this email.",
            resetLink
        );
        
        emailProvider.sendEmail(email, subject, body);
        log.info("Password reset email sent to: {}", email);
    }

    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        String redisKey = RESET_KEY_PREFIX + token;
        String email = redisTemplate.opsForValue().get(redisKey);
        
        if (email == null) {
            throw new IllegalArgumentException("Invalid or expired reset token");
        }
        
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        redisTemplate.delete(redisKey);
        log.info("Password reset successfully for user: {}", email);
    }

    private String generateOtpCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private String generatePlaceholderToken(UUID userId) {
        return "mock-jwt-token-" + userId.toString();
    }
}

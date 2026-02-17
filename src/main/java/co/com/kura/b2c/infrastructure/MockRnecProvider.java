package co.com.kura.b2c.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockRnecProvider implements IdentityVerificationProvider {

    // MOCK INTEGRATION: RNEC - Always returns MATCH
    @Override
    public VerificationResult verify(String cedula, String fullName) {
        log.info("[MOCK RNEC] Verifying identity for cedula: {}, name: {}", cedula, fullName);
        return new VerificationResult(true, "Identity verified successfully (mock)");
    }
}

package co.com.kura.b2c.infrastructure;

public interface IdentityVerificationProvider {
    VerificationResult verify(String cedula, String fullName);
}

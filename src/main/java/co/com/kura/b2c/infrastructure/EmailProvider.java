package co.com.kura.b2c.infrastructure;

public interface EmailProvider {
    void sendEmail(String to, String subject, String body);
}

package co.com.kura.b2c.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockSesEmailProvider implements EmailProvider {

    // MOCK INTEGRATION: AWS SES - Logs email to console
    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("[MOCK AWS SES] Sending email to: {}", to);
        log.info("[MOCK AWS SES] Subject: {}", subject);
        log.info("[MOCK AWS SES] Body: {}", body);
        log.info("[MOCK AWS SES] Email sent successfully (mock)");
    }
}

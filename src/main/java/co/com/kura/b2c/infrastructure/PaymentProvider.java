package co.com.kura.b2c.infrastructure;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentProvider {
    PaymentResult createPayment(UUID orderId, BigDecimal amount, String currency, String description);
}

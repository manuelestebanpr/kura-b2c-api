package co.com.kura.b2c.infrastructure;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResult(String externalId, String checkoutUrl, String status) {
}

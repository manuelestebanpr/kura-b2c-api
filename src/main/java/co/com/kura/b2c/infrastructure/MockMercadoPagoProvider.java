package co.com.kura.b2c.infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class MockMercadoPagoProvider implements PaymentProvider {

    // MOCK INTEGRATION: MercadoPago - Returns fake preference ID
    @Override
    public PaymentResult createPayment(UUID orderId, BigDecimal amount, String currency, String description) {
        String externalId = "MOCK-" + orderId.toString();
        String checkoutUrl = "https://sandbox.mercadopago.com.co/checkout/v1/redirect?pref_id=" + externalId;
        
        log.info("[MOCK MercadoPago] Creating payment for order: {}, amount: {} {}", orderId, amount, currency);
        log.info("[MOCK MercadoPago] External ID: {}, Checkout URL: {}", externalId, checkoutUrl);
        
        return new PaymentResult(externalId, checkoutUrl, "PENDING");
    }
}

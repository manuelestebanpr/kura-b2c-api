package co.com.kura.b2c.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private UUID id;
    private String provider;
    private String externalId;
    private String checkoutUrl;
    private BigDecimal amount;
    private String currency;
    private String status;
}

package co.com.kura.b2c.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private UUID id;
    private String orderNumber;
    private String posName;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemResponse> items;
    private WalkinTicketResponse walkinTicket;
    private PaymentResponse payment;
    private OffsetDateTime createdAt;
}

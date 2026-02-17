package co.com.kura.b2c.api.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderRequest {

    private UUID posId;
    private String guestEmail;
    private String guestCedula;
    private List<OrderItemRequest> items;
}

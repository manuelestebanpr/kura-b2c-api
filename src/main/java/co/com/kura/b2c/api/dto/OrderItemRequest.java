package co.com.kura.b2c.api.dto;

import lombok.Data;

@Data
public class OrderItemRequest {

    private String masterServiceCode;
    private int quantity;
}

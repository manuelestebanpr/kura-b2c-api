package co.com.kura.b2c.api.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class PosResponse {
    private UUID id;
    private String name;
    private String address;
    private String city;
    private String department;
}
